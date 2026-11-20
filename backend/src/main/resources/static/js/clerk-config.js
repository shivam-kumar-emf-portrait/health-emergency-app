// Authentication Configuration Module
// Direct REST JWT Authentication for Citizen, Hospital SaaS & Super Admin
const ClerkAuth = {
  openSignIn(role) {
    if (role === 'hospital_admin' || role === 'hospital') {
      window.location.href = 'login.html?tab=hospital';
    } else if (role === 'admin') {
      window.location.href = 'login.html?tab=admin';
    } else {
      window.location.href = 'login.html?tab=patient';
    }
  },
  openSignUp(role) {
    this.openSignIn(role);
  },
  signOut() {
    localStorage.removeItem('token');
    localStorage.removeItem('userName');
    localStorage.removeItem('userRole');
    localStorage.removeItem('hospitalToken');
    localStorage.removeItem('hospitalName');
    window.location.href = 'login.html';
  }
};
