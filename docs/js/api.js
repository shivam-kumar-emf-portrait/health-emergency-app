/**
 * Lifeline Health Emergency & Hospital SaaS - Unified API Client
 */
const API_BASE_URL = (() => {
    // If opened directly as a file or from an external dev server, point to Spring Boot on 8080
    if (window.location.protocol === 'file:' || window.location.port !== '8080') {
        return 'http://localhost:8080';
    }
    return window.location.origin;
})();

const SESSION_KEYS = {
    TOKEN: 'lifeline_token',
    ROLE: 'lifeline_role',
    NAME: 'lifeline_name',
    EMAIL: 'lifeline_email',
    ID: 'lifeline_id'
};

const API = {
    BASE_URL: API_BASE_URL,

    setSession(authData) {
        this.clearSession();
        if (!authData || !authData.token) return;

        localStorage.setItem(SESSION_KEYS.TOKEN, authData.token);
        if (authData.role) localStorage.setItem(SESSION_KEYS.ROLE, authData.role);
        if (authData.name) localStorage.setItem(SESSION_KEYS.NAME, authData.name);
        if (authData.email) localStorage.setItem(SESSION_KEYS.EMAIL, authData.email);
        if (authData.id != null) localStorage.setItem(SESSION_KEYS.ID, String(authData.id));

        // Sync legacy keys for backward compatibility
        if (authData.role === 'ROLE_HOSPITAL_ADMIN') {
            localStorage.setItem('hospitalToken', authData.token);
            localStorage.setItem('hospitalName', authData.name || 'Hospital ERP');
        } else {
            localStorage.setItem('token', authData.token);
            localStorage.setItem('userName', authData.name || 'User');
            localStorage.setItem('userRole', authData.role || 'ROLE_USER');
        }
    },

    clearSession() {
        localStorage.removeItem(SESSION_KEYS.TOKEN);
        localStorage.removeItem(SESSION_KEYS.ROLE);
        localStorage.removeItem(SESSION_KEYS.NAME);
        localStorage.removeItem(SESSION_KEYS.EMAIL);
        localStorage.removeItem(SESSION_KEYS.ID);
        // Clear legacy keys too
        localStorage.removeItem('token');
        localStorage.removeItem('hospitalToken');
        localStorage.removeItem('userName');
        localStorage.removeItem('hospitalName');
        localStorage.removeItem('userRole');
    },

    getSession() {
        const token = localStorage.getItem(SESSION_KEYS.TOKEN) || localStorage.getItem('token') || localStorage.getItem('hospitalToken');
        if (!token) return null;

        let role = localStorage.getItem(SESSION_KEYS.ROLE) || localStorage.getItem('userRole');
        if (!role) {
            role = localStorage.getItem('hospitalToken') ? 'ROLE_HOSPITAL_ADMIN' : 'ROLE_USER';
        }

        const name = localStorage.getItem(SESSION_KEYS.NAME) || localStorage.getItem('userName') || localStorage.getItem('hospitalName') || 'User';
        const email = localStorage.getItem(SESSION_KEYS.EMAIL) || '';
        const id = localStorage.getItem(SESSION_KEYS.ID) || null;

        return { token, role, name, email, id };
    },

    getToken() {
        const session = this.getSession();
        return session ? session.token : null;
    },

    logout(redirectUrl = 'login.html') {
        this.clearSession();
        window.location.replace(redirectUrl);
    },

    guardRoute(allowedRoles = [], fallbackLoginUrl = 'login.html') {
        const session = this.getSession();

        if (!session) {
            alert('Sign In Required: Please sign in to access this page.');
            window.location.replace(fallbackLoginUrl);
            return false;
        }

        if (allowedRoles.length > 0 && !allowedRoles.includes(session.role)) {
            if (session.role === 'ROLE_HOSPITAL_ADMIN') {
                alert('Access Denied: You are signed in as Hospital Staff. Hospital accounts cannot access this portal. Redirecting to your Hospital ERP.');
                window.location.replace('hospital-dashboard.html');
            } else if (session.role === 'ROLE_SUPER_ADMIN') {
                alert('Access Denied: You are signed in as Super Admin. Super Admins oversee facilities via the Admin Portal. Redirecting to Admin Portal.');
                window.location.replace('admin.html');
            } else {
                // Citizen Patient
                alert('Access Denied: You are signed in as a Citizen Patient. Patient accounts cannot access this portal. Redirecting to Emergency Finder.');
                window.location.replace('emergency.html');
            }
            return false;
        }

        return true;
    },

    getHeaders(customToken) {
        const token = customToken || this.getToken();
        const headers = {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        };
        if (token) {
            headers['Authorization'] = 'Bearer ' + token;
        }
        return headers;
    },

    async request(endpoint, options = {}) {
        const url = `${this.BASE_URL}${endpoint}`;
        const headers = this.getHeaders(options.token);

        const config = {
            ...options,
            headers: {
                ...headers,
                ...(options.headers || {})
            }
        };

        try {
            const response = await fetch(url, config);
            const contentType = response.headers.get('content-type');
            let data = null;
            if (contentType && contentType.includes('application/json')) {
                data = await response.json();
            } else {
                data = await response.text();
            }

            if (!response.ok) {
                const errorMessage = (data && data.message) || (typeof data === 'string' ? data : 'Request failed with status ' + response.status);
                throw new Error(errorMessage);
            }

            return data;
        } catch (error) {
            console.error(`[API Error] ${options.method || 'GET'} ${endpoint}:`, error);
            throw error;
        }
    },

    get(endpoint, token) {
        return this.request(endpoint, { method: 'GET', token });
    },

    post(endpoint, body, token) {
        return this.request(endpoint, {
            method: 'POST',
            body: JSON.stringify(body),
            token
        });
    },

    put(endpoint, body, token) {
        return this.request(endpoint, {
            method: 'PUT',
            body: JSON.stringify(body),
            token
        });
    },

    patch(endpoint, body, token) {
        return this.request(endpoint, {
            method: 'PATCH',
            body: JSON.stringify(body),
            token
        });
    },

    delete(endpoint, token) {
        return this.request(endpoint, { method: 'DELETE', token });
    },

    // Emergency APIs
    findHospitals(latitude, longitude, emergencyType, locationName) {
        return this.post('/api/emergency/find-hospitals', {
            latitude,
            longitude,
            emergencyType,
            locationName
        });
    },

    notifyHospital(payload) {
        return this.post('/api/emergency/notify', payload);
    },

    getTransitAiPlan(payload) {
        return this.post('/api/emergency/transit-ai-plan', payload);
    },

    getEmergencyTypes() {
        return this.get('/api/emergency/types');
    },

    geocodeLocation(query) {
        return this.get(`/api/emergency/geocode?query=${encodeURIComponent(query)}`);
    },

    suggestLocations(query) {
        return this.get(`/api/emergency/suggest-locations?query=${encodeURIComponent(query)}`);
    },

    // AI Clinical Triage Chat APIs (Works for both guest & logged in)
    sendChatMessage(message, history) {
        return this.post('/api/chat', { message, history });
    },

    getChatHistory() {
        return this.get('/api/chat/history');
    },

    // Auth APIs
    loginUser(email, password) {
        return this.post('/api/auth/login', { email, password });
    },

    registerUser(payload) {
        return this.post('/api/auth/register', payload);
    },

    loginHospital(email, password) {
        return this.post('/api/auth/hospital/login', { email, password });
    },

    registerHospital(payload) {
        return this.post('/api/auth/hospital/register', payload);
    },

    // Hospital SaaS ERP APIs
    getHospitalProfile() {
        return this.get('/api/hospital/me', localStorage.getItem('hospitalToken'));
    },

    updateOperations(data) {
        return this.patch('/api/hospital/operations', data, localStorage.getItem('hospitalToken'));
    },

    toggleHospitalOpen() {
        return this.patch('/api/hospital/toggle-open', {}, localStorage.getItem('hospitalToken'));
    },

    getHospitalDoctors() {
        return this.get('/api/hospital/doctors', localStorage.getItem('hospitalToken'));
    },

    addDoctor(doctorData) {
        return this.post('/api/hospital/doctors', doctorData, localStorage.getItem('hospitalToken'));
    },

    toggleDoctorDuty(doctorId) {
        return this.patch(`/api/hospital/doctors/${doctorId}/toggle-duty`, {}, localStorage.getItem('hospitalToken'));
    },

    deleteDoctor(doctorId) {
        return this.delete(`/api/hospital/doctors/${doctorId}`, localStorage.getItem('hospitalToken'));
    },

    getHospitalEmergencies() {
        return this.get('/api/hospital/emergencies', localStorage.getItem('hospitalToken'));
    },

    updateEmergencyStatus(caseId, status) {
        return this.patch(`/api/hospital/emergencies/${caseId}/status`, { status }, localStorage.getItem('hospitalToken'));
    },

    // SaaS: Patient EHR Records
    getHospitalPatients(status) {
        const query = status ? `?status=${encodeURIComponent(status)}` : '';
        return this.get(`/api/hospital/patients${query}`, localStorage.getItem('hospitalToken'));
    },

    getPatient(patientId) {
        return this.get(`/api/hospital/patients/${patientId}`, localStorage.getItem('hospitalToken'));
    },

    admitPatient(payload) {
        return this.post('/api/hospital/patients', payload, localStorage.getItem('hospitalToken'));
    },

    updatePatientStatus(patientId, status, bedNumber) {
        return this.patch(`/api/hospital/patients/${patientId}/status`, { status, bedNumber }, localStorage.getItem('hospitalToken'));
    },

    dischargePatient(patientId) {
        return this.post(`/api/hospital/patients/${patientId}/discharge`, {}, localStorage.getItem('hospitalToken'));
    },

    // SaaS: Billing & Invoicing
    getHospitalBills() {
        return this.get('/api/hospital/billing', localStorage.getItem('hospitalToken'));
    },

    getBill(billId) {
        return this.get(`/api/hospital/billing/${billId}`, localStorage.getItem('hospitalToken'));
    },

    createBill(payload) {
        return this.post('/api/hospital/billing', payload, localStorage.getItem('hospitalToken'));
    },

    updateBillPayment(billId, paymentStatus, paymentMethod) {
        return this.patch(`/api/hospital/billing/${billId}/status`, { paymentStatus, paymentMethod }, localStorage.getItem('hospitalToken'));
    },

    // SaaS: Staff Attendance
    getStaffAttendance(date) {
        const query = date ? `?date=${encodeURIComponent(date)}` : '';
        return this.get(`/api/hospital/attendance${query}`, localStorage.getItem('hospitalToken'));
    },

    logStaffAttendance(payload) {
        return this.post('/api/hospital/attendance', payload, localStorage.getItem('hospitalToken'));
    },

    checkOutStaff(attendanceId) {
        return this.patch(`/api/hospital/attendance/${attendanceId}/checkout`, {}, localStorage.getItem('hospitalToken'));
    },

    // SaaS: Pharmacy & Emergency Medicine Stocks
    getPharmacyMedicines() {
        return this.get('/api/hospital/pharmacy', localStorage.getItem('hospitalToken'));
    },

    updateMedicineStock(medicineId, unitsInStock) {
        return this.patch(`/api/hospital/pharmacy/${medicineId}/stock`, { unitsInStock }, localStorage.getItem('hospitalToken'));
    },

    addMedicine(payload) {
        return this.post('/api/hospital/pharmacy', payload, localStorage.getItem('hospitalToken'));
    },

    getPublicHospital(id) {
        return this.get(`/api/hospital/public/${id}`);
    },

    // Super Admin Governance APIs
    getAdminStats() {
        return this.get('/api/admin/stats');
    },

    getAdminHospitals(search = '', status = 'ALL') {
        const query = `?search=${encodeURIComponent(search)}&status=${encodeURIComponent(status)}`;
        return this.get(`/api/admin/hospitals${query}`);
    },

    verifyHospital(hospitalId, notes = '') {
        return this.post(`/api/admin/hospitals/${hospitalId}/verify`, { notes });
    },

    unverifyHospital(hospitalId, reason = '') {
        return this.post(`/api/admin/hospitals/${hospitalId}/unverify`, { reason });
    },

    toggleHospitalSuspend(hospitalId, reason = '') {
        return this.post(`/api/admin/hospitals/${hospitalId}/toggle-suspend`, { reason });
    },

    closeHospitalPermanently(hospitalId, reason = '') {
        return this.post(`/api/admin/hospitals/${hospitalId}/close-permanently`, { reason });
    },

    undoHospitalClosure(hospitalId, reason = '') {
        return this.post(`/api/admin/hospitals/${hospitalId}/undo-closure`, { reason });
    },

    getAdminUsers(search = '', status = 'ALL') {
        const query = `?search=${encodeURIComponent(search)}&status=${encodeURIComponent(status)}`;
        return this.get(`/api/admin/users${query}`);
    },

    toggleBlockUser(userId, reason = '') {
        return this.post(`/api/admin/users/${userId}/toggle-block`, { reason });
    },

    getAdminReports(type = 'ALL', status = 'ALL', severity = 'ALL') {
        const query = `?type=${encodeURIComponent(type)}&status=${encodeURIComponent(status)}&severity=${encodeURIComponent(severity)}`;
        return this.get(`/api/admin/reports${query}`);
    },

    actionReport(reportId, status, adminAction, adminNotes, blockTarget = false) {
        return this.post(`/api/admin/reports/${reportId}/action`, {
            status,
            adminAction,
            adminNotes,
            blockTarget
        });
    },

    getAdminWorkforce(hospitalId = null, role = 'ALL', department = '') {
        let query = `?role=${encodeURIComponent(role)}&department=${encodeURIComponent(department)}`;
        if (hospitalId) query += `&hospitalId=${encodeURIComponent(hospitalId)}`;
        return this.get(`/api/admin/workforce${query}`);
    },

    toggleStaffDuty(role, id) {
        return this.post('/api/admin/workforce/toggle-duty', { role, id });
    },

    // Two-Way Feedback & Reporting APIs
    submitFeedbackReport(payload) {
        return this.post('/api/feedback/submit', payload);
    },

    getHospitalReviews(hospitalId) {
        return this.get(`/api/feedback/hospital/${hospitalId}`);
    },

    // Citizen Profile, Alert History & Hospital Reviews APIs
    getUserProfile() {
        return this.get('/api/user/me');
    },

    getEligibleReviewHospitals() {
        return this.get('/api/user/eligible-reviews');
    },

    updateUserProfile(data) {
        return this.put('/api/user/profile', data);
    },

    submitHospitalReview(payload) {
        return this.post('/api/user/feedback', payload);
    }
};

