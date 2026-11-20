/**
 * ============================================================================
 * LIFELINE GRAVITATIONAL RADAR & RESOURCE KINETICS ENGINE
 * High-Level Physics Simulation: Newton's Gravity, Hooke's Law Springs,
 * Wave Propagators, and Magnetic Cursor Dynamics.
 * ============================================================================
 */
class EmergencyPhysicsRadar {
    constructor(canvasId) {
        this.canvas = document.getElementById(canvasId);
        if (!this.canvas) return;
        this.ctx = this.canvas.getContext('2d');
        this.nodes = [];
        this.particles = [];
        this.shockwaves = [];
        this.mouse = { x: -1000, y: -1000, vx: 0, vy: 0, lastX: 0, lastY: 0, active: false };
        this.G = 0.8; // Gravitational constant
        this.damping = 0.985;
        this.springK = 0.003;
        this.time = 0;

        this.initDimensions();
        this.initHospitals();
        this.initParticles();
        this.bindEvents();
        this.animate();
    }

    initDimensions() {
        const rect = this.canvas.parentElement.getBoundingClientRect();
        this.width = this.canvas.width = rect.width;
        this.height = this.canvas.height = rect.height || 420;
    }

    initHospitals() {
        const cx = this.width / 2;
        const cy = this.height / 2;
        const radius = Math.min(this.width, this.height) * 0.35;

        const hospitalData = [
            { name: "Metro Heart", specialty: "Cardiology", mass: 28, color: "#e76f51", beds: 18, icu: 6, angle: 0 },
            { name: "Apex Neuro", specialty: "Neurology", mass: 24, color: "#0d9488", beds: 22, icu: 8, angle: (2 * Math.PI) / 5 },
            { name: "City Care", specialty: "Emergency", mass: 20, color: "#10b981", beds: 14, icu: 2, angle: (4 * Math.PI) / 5 },
            { name: "Mother & Child", specialty: "Maternity", mass: 18, color: "#f4a261", beds: 16, icu: 5, angle: (6 * Math.PI) / 5 },
            { name: "LifeLine Super", specialty: "Trauma ICU", mass: 32, color: "#14b8a6", beds: 35, icu: 9, angle: (8 * Math.PI) / 5 }
        ];

        this.nodes = hospitalData.map(h => {
            const x = cx + radius * Math.cos(h.angle) + (Math.random() * 20 - 10);
            const y = cy + radius * Math.sin(h.angle) + (Math.random() * 20 - 10);
            return {
                ...h,
                x, y,
                homeX: x, homeY: y,
                vx: 0, vy: 0,
                pulse: 0
            };
        });
    }

    initParticles() {
        this.particles = [];
        const count = 45;
        for (let i = 0; i < count; i++) {
            const targetNode = this.nodes[Math.floor(Math.random() * this.nodes.length)];
            const dist = 30 + Math.random() * 120;
            const angle = Math.random() * Math.PI * 2;
            this.particles.push({
                x: targetNode.x + dist * Math.cos(angle),
                y: targetNode.y + dist * Math.sin(angle),
                vx: -Math.sin(angle) * (1.2 + Math.random() * 0.8),
                vy: Math.cos(angle) * (1.2 + Math.random() * 0.8),
                radius: 1.5 + Math.random() * 2,
                color: Math.random() > 0.5 ? '#34d399' : (Math.random() > 0.5 ? '#f4a261' : '#2dd4bf'),
                targetNode
            });
        }
    }

    triggerEmergencyPulse(x, y, color = "#e76f51") {
        this.shockwaves.push({
            x, y,
            radius: 5,
            maxRadius: 180,
            opacity: 0.9,
            color
        });
    }

    bindEvents() {
        window.addEventListener('resize', () => {
            this.initDimensions();
            this.initHospitals();
        });

        this.canvas.addEventListener('mousemove', (e) => {
            const rect = this.canvas.getBoundingClientRect();
            const currentX = e.clientX - rect.left;
            const currentY = e.clientY - rect.top;
            this.mouse.vx = currentX - this.mouse.lastX;
            this.mouse.vy = currentY - this.mouse.lastY;
            this.mouse.lastX = this.mouse.x = currentX;
            this.mouse.lastY = this.mouse.y = currentY;
            this.mouse.active = true;
        });

        this.canvas.addEventListener('mouseleave', () => {
            this.mouse.active = false;
            this.mouse.x = -1000;
            this.mouse.y = -1000;
        });

        this.canvas.addEventListener('click', (e) => {
            const rect = this.canvas.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;
            this.triggerEmergencyPulse(x, y, '#e76f51');

            // Find closest hospital and pulse it
            let closest = null;
            let minDist = Infinity;
            this.nodes.forEach(n => {
                const d = Math.hypot(n.x - x, n.y - y);
                if (d < minDist) {
                    minDist = d;
                    closest = n;
                }
            });
            if (closest && minDist < 150) {
                closest.pulse = 1.0;
            }
        });
    }

    updatePhysics() {
        this.time += 0.02;

        // 1. Update Hospital Nodes (Hooke's Spring to Home + Inter-node Repulsion + Mouse Magnetism)
        for (let i = 0; i < this.nodes.length; i++) {
            const n = this.nodes[i];

            // Spring force back to home position: F = -k * (x - home) - c * v
            const sfx = -this.springK * (n.x - n.homeX) - 0.05 * n.vx;
            const sfy = -this.springK * (n.y - n.homeY) - 0.05 * n.vy;
            n.vx += sfx;
            n.vy += sfy;

            // Cursor magnetic force
            if (this.mouse.active) {
                const dx = n.x - this.mouse.x;
                const dy = n.y - this.mouse.y;
                const dist = Math.hypot(dx, dy) + 1;
                if (dist < 180) {
                    const force = (180 - dist) * 0.0006;
                    n.vx += (dx / dist) * force * 15;
                    n.vy += (dy / dist) * force * 15;
                }
            }

            // Damping & position update
            n.vx *= this.damping;
            n.vy *= this.damping;
            n.x += n.vx;
            n.y += n.vy;

            // Pulse decay
            if (n.pulse > 0) n.pulse = Math.max(0, n.pulse - 0.02);
        }

        // 2. Update Orbiting Particles (Gravitational attraction to hospitals)
        for (let p of this.particles) {
            let totalFx = 0;
            let totalFy = 0;

            for (let n of this.nodes) {
                const dx = n.x - p.x;
                const dy = n.y - p.y;
                const distSq = dx * dx + dy * dy + 400; // softening factor
                const dist = Math.sqrt(distSq);
                const F = (this.G * n.mass) / distSq;
                totalFx += (dx / dist) * F;
                totalFy += (dy / dist) * F;
            }

            // Mouse turbulence
            if (this.mouse.active) {
                const mdx = p.x - this.mouse.x;
                const mdy = p.y - this.mouse.y;
                const mdist = Math.hypot(mdx, mdy) + 1;
                if (mdist < 120) {
                    const repulse = (120 - mdist) * 0.003;
                    totalFx += (mdx / mdist) * repulse + this.mouse.vx * 0.02;
                    totalFy += (mdy / mdist) * repulse + this.mouse.vy * 0.02;
                }
            }

            p.vx = (p.vx + totalFx) * 0.99;
            p.vy = (p.vy + totalFy) * 0.99;
            p.x += p.vx;
            p.y += p.vy;

            // Soft boundary reflection
            if (p.x < 10) { p.x = 10; p.vx *= -0.8; }
            if (p.x > this.width - 10) { p.x = this.width - 10; p.vx *= -0.8; }
            if (p.y < 10) { p.y = 10; p.vy *= -0.8; }
            if (p.y > this.height - 10) { p.y = this.height - 10; p.vy *= -0.8; }
        }

        // 3. Update Shockwaves
        for (let i = this.shockwaves.length - 1; i >= 0; i--) {
            const sw = this.shockwaves[i];
            sw.radius += 2.2;
            sw.opacity = Math.max(0, 0.9 * (1 - sw.radius / sw.maxRadius));
            if (sw.radius >= sw.maxRadius) {
                this.shockwaves.splice(i, 1);
            }
        }

        // Periodic ambient pulse
        if (Math.random() < 0.012 && this.nodes.length > 0) {
            const randomNode = this.nodes[Math.floor(Math.random() * this.nodes.length)];
            this.triggerEmergencyPulse(randomNode.x, randomNode.y, randomNode.color);
        }
    }

    render() {
        this.ctx.clearRect(0, 0, this.width, this.height);

        // 1. Draw Mesh Tension Springs between Hospitals
        this.ctx.lineWidth = 1;
        for (let i = 0; i < this.nodes.length; i++) {
            for (let j = i + 1; j < this.nodes.length; j++) {
                const a = this.nodes[i];
                const b = this.nodes[j];
                const dist = Math.hypot(a.x - b.x, a.y - b.y);
                const alpha = Math.max(0.04, 0.25 - dist / 600);
                this.ctx.strokeStyle = `rgba(148, 163, 184, ${alpha})`;
                this.ctx.beginPath();
                this.ctx.moveTo(a.x, a.y);
                this.ctx.lineTo(b.x, b.y);
                this.ctx.stroke();
            }
        }

        // 2. Draw Shockwaves
        for (let sw of this.shockwaves) {
            this.ctx.save();
            this.ctx.beginPath();
            this.ctx.arc(sw.x, sw.y, sw.radius, 0, Math.PI * 2);
            this.ctx.strokeStyle = sw.color;
            this.ctx.globalAlpha = sw.opacity;
            this.ctx.lineWidth = 1.5;
            this.ctx.stroke();
            this.ctx.restore();
        }

        // 3. Draw Particles (Resource Corpuscles)
        for (let p of this.particles) {
            this.ctx.beginPath();
            this.ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2);
            this.ctx.fillStyle = p.color;
            this.ctx.globalAlpha = 0.75;
            this.ctx.fill();
        }

        // 4. Draw Hospital Nodes
        for (let n of this.nodes) {
            // Pulse wave
            if (n.pulse > 0) {
                this.ctx.save();
                this.ctx.beginPath();
                this.ctx.arc(n.x, n.y, n.mass + n.pulse * 25, 0, Math.PI * 2);
                this.ctx.strokeStyle = n.color;
                this.ctx.globalAlpha = n.pulse * 0.6;
                this.ctx.lineWidth = 2;
                this.ctx.stroke();
                this.ctx.restore();
            }

            // Outer node body (Crisp White Card Node with specialty border)
            this.ctx.save();
            this.ctx.beginPath();
            this.ctx.arc(n.x, n.y, n.mass * 0.6, 0, Math.PI * 2);
            this.ctx.fillStyle = '#ffffff';
            this.ctx.strokeStyle = n.color;
            this.ctx.lineWidth = 2.5;
            this.ctx.shadowColor = 'rgba(15, 23, 42, 0.12)';
            this.ctx.shadowBlur = 8;
            this.ctx.fill();
            this.ctx.stroke();

            // Inner core indicator
            this.ctx.beginPath();
            this.ctx.arc(n.x, n.y, 4.5, 0, Math.PI * 2);
            this.ctx.fillStyle = n.color;
            this.ctx.fill();

            // Label text (Clean Charcoal Typography)
            this.ctx.shadowBlur = 0;
            this.ctx.fillStyle = '#0f172a';
            this.ctx.font = '700 11.5px -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif';
            this.ctx.textAlign = 'center';
            this.ctx.fillText(n.name, n.x, n.y + n.mass * 0.6 + 15);

            this.ctx.fillStyle = '#64748b';
            this.ctx.font = '600 9.5px -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif';
            this.ctx.fillText(`${n.icu} ICU • ${n.beds} Beds`, n.x, n.y + n.mass * 0.6 + 27);
            this.ctx.restore();
        }
    }

    animate() {
        this.updatePhysics();
        this.render();
        requestAnimationFrame(() => this.animate());
    }
}

// 3D Physics Inertial Card Tilt
document.addEventListener('DOMContentLoaded', () => {
    new EmergencyPhysicsRadar('physicsRadarCanvas');

    // Attach 3D tilt physics to cards
    const tiltCards = document.querySelectorAll('.tilt-card');
    tiltCards.forEach(card => {
        let bounds;
        let isHovered = false;

        card.addEventListener('mouseenter', () => {
            bounds = card.getBoundingClientRect();
            isHovered = true;
        });

        card.addEventListener('mousemove', (e) => {
            if (!bounds) bounds = card.getBoundingClientRect();
            const mouseX = e.clientX - bounds.left;
            const mouseY = e.clientY - bounds.top;
            const xPct = (mouseX / bounds.width - 0.5) * 2;
            const yPct = (mouseY / bounds.height - 0.5) * 2;
            card.style.transform = `perspective(800px) rotateX(${-yPct * 7}deg) rotateY(${xPct * 7}deg) translateY(-4px)`;
        });

        card.addEventListener('mouseleave', () => {
            isHovered = false;
            card.style.transform = `perspective(800px) rotateX(0deg) rotateY(0deg) translateY(0px)`;
        });
    });
});

