/**
 * ==========================================================================
 * PLATAFORMA DE GESTIÓN DE CITAS - MÓDULO DE RESERVAS
 * Manejo de servicios, calendario dinámico, slots y tema claro/oscuro
 * ==========================================================================
 */

document.addEventListener('DOMContentLoaded', () => {
    App.init();
});

const App = {
    // Configuración base de la API Spring Boot
    config: {
        apiBaseUrl: window.location.origin.includes('localhost') || window.location.origin.includes('127.0.0.1')
            ? (window.location.port === '8080' ? window.location.origin : 'http://localhost:8080')
            : 'http://localhost:8080'
    },

    // Estado reactivo de la aplicación
    state: {
        servicios: [],
        clientes: [],
        selectedServicioId: null,
        selectedFecha: null,
        selectedSlot: null,
        clienteId: 1, // ID del cliente seleccionado
        isLoadingSlots: false,
        isSubmitting: false
    },

    // Elementos del DOM cacheados
    dom: {},

    // Inicialización del módulo
    init() {
        this.cacheDom();
        this.initTheme();
        this.initDatePicker();
        this.bindEvents();
        this.loadServicios();
        this.loadClientes();
    },

    // Cache de selectores DOM
    cacheDom() {
        this.dom = {
            themeToggleBtn: document.getElementById('themeToggle') || document.getElementById('themeToggleBtn'),
            themeIcon: document.getElementById('themeIcon'),
            
            // Formularios y controles
            servicioSelect: document.getElementById('servicioSelect'),
            servicePreview: document.getElementById('servicePreview'),
            previewDesc: document.getElementById('previewDesc'),
            previewCategory: document.getElementById('previewCategory'),
            previewDuration: document.getElementById('previewDuration'),
            previewPrice: document.getElementById('previewPrice'),
            
            fechaInput: document.getElementById('fechaInput'),
            quickDatesContainer: document.getElementById('quickDatesContainer'),
            
            slotsContainer: document.getElementById('slotsContainer'),
            slotsGrid: document.getElementById('slotsGrid'),
            slotsEmptyState: document.getElementById('slotsEmptyState'),
            slotsLoading: document.getElementById('slotsLoading'),
            
            clienteSelect: document.getElementById('clienteSelect'),
            clienteIdInput: document.getElementById('clienteIdInput'),
            
            // Resumen de reserva
            bookingSummary: document.getElementById('bookingSummary'),
            summaryServicio: document.getElementById('summaryServicio'),
            summaryFecha: document.getElementById('summaryFecha'),
            summaryHora: document.getElementById('summaryHora'),
            summaryEspecialista: document.getElementById('summaryEspecialista'),
            summaryPrecio: document.getElementById('summaryPrecio'),
            
            // Botón de acción y alertas
            btnReservar: document.getElementById('btnReservar'),
            btnText: document.getElementById('btnText'),
            btnSpinner: document.getElementById('btnSpinner'),
            
            alertSuccess: document.getElementById('alertSuccess'),
            alertSuccessMsg: document.getElementById('alertSuccessMsg'),
            alertDanger: document.getElementById('alertDanger'),
            alertDangerMsg: document.getElementById('alertDangerMsg')
        };
    },

    // ──────────────────────────────────────────────────────────────────────────
    // GESTIÓN DEL TEMA (DARK / LIGHT MODE)
    // ──────────────────────────────────────────────────────────────────────────
    initTheme() {
        const savedTheme = localStorage.getItem('auth-theme') || 
            (window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light');
        
        this.applyTheme(savedTheme);
    },

    applyTheme(theme) {
        document.documentElement.setAttribute('data-theme', theme);
        localStorage.setItem('auth-theme', theme);
        this.updateThemeIcon(theme);
    },

    toggleTheme() {
        const currentTheme = document.documentElement.getAttribute('data-theme') || 'light';
        const nextTheme = currentTheme === 'dark' ? 'light' : 'dark';
        this.applyTheme(nextTheme);
    },

    updateThemeIcon(theme) {
        if (!this.dom.themeIcon) return;
        this.dom.themeIcon.className = theme === 'dark' ? 'bi bi-moon-stars-fill' : 'bi bi-sun-fill';
    },

    // ──────────────────────────────────────────────────────────────────────────
    // INICIALIZACIÓN DE FECHAS (BLOQUEO DE FECHAS PASADAS)
    // ──────────────────────────────────────────────────────────────────────────
    initDatePicker() {
        const today = new Date();
        const yyyy = today.getFullYear();
        const mm = String(today.getMonth() + 1).padStart(2, '0');
        const dd = String(today.getDate()).padStart(2, '0');
        const minDate = `${yyyy}-${mm}-${dd}`;

        this.dom.fechaInput.setAttribute('min', minDate);
        this.dom.fechaInput.value = minDate;
        this.state.selectedFecha = minDate;

        this.renderQuickDates(today);
    },

    renderQuickDates(today) {
        if (!this.dom.quickDatesContainer) return;
        
        this.dom.quickDatesContainer.innerHTML = '';
        
        const chips = [
            { label: 'Hoy', offset: 0 },
            { label: 'Mañana', offset: 1 },
            { label: '+2 Días', offset: 2 },
            { label: '+3 Días', offset: 3 }
        ];

        chips.forEach((chip, index) => {
            const date = new Date(today);
            date.setDate(today.getDate() + chip.offset);
            
            const yyyy = date.getFullYear();
            const mm = String(date.getMonth() + 1).padStart(2, '0');
            const dd = String(date.getDate()).padStart(2, '0');
            const iso = `${yyyy}-${mm}-${dd}`;

            const btn = document.createElement('button');
            btn.type = 'button';
            btn.className = `quick-date-chip ${index === 0 ? 'active' : ''}`;
            btn.textContent = chip.label;
            btn.dataset.date = iso;
            
            btn.addEventListener('click', () => {
                document.querySelectorAll('.quick-date-chip').forEach(c => c.classList.remove('active'));
                btn.classList.add('active');
                this.dom.fechaInput.value = iso;
                this.onFechaChange(iso);
            });

            this.dom.quickDatesContainer.appendChild(btn);
        });
    },

    // ──────────────────────────────────────────────────────────────────────────
    // REGISTRO DE EVENTOS
    // ──────────────────────────────────────────────────────────────────────────
    bindEvents() {
        if (this.dom.themeToggleBtn) {
            this.dom.themeToggleBtn.addEventListener('click', () => this.toggleTheme());
        }

        this.dom.servicioSelect.addEventListener('change', (e) => {
            this.onServicioChange(e.target.value);
        });

        this.dom.fechaInput.addEventListener('change', (e) => {
            this.onFechaChange(e.target.value);
            // Actualizar active en chips si coincide
            document.querySelectorAll('.quick-date-chip').forEach(c => {
                c.classList.toggle('active', c.dataset.date === e.target.value);
            });
        });

        if (this.dom.clienteSelect) {
            this.dom.clienteSelect.addEventListener('change', (e) => {
                this.state.clienteId = Number(e.target.value) || 1;
                if (this.dom.clienteIdInput) this.dom.clienteIdInput.value = this.state.clienteId;
            });
        }

        if (this.dom.clienteIdInput) {
            this.dom.clienteIdInput.addEventListener('input', (e) => {
                this.state.clienteId = Number(e.target.value) || 1;
            });
        }

        this.dom.btnReservar.addEventListener('click', () => this.submitReserva());
    },

    // ──────────────────────────────────────────────────────────────────────────
    // CARGA DE CLIENTES REGISTRADOS
    // ──────────────────────────────────────────────────────────────────────────
    async loadClientes() {
        if (!this.dom.clienteSelect) return;
        try {
            const response = await fetch(`${this.config.apiBaseUrl}/api/clientes`);
            if (response.ok) {
                const clientes = await response.json();
                this.state.clientes = clientes;
                
                if (clientes.length > 0) {
                    this.dom.clienteSelect.innerHTML = '';
                    clientes.forEach(c => {
                        const opt = document.createElement('option');
                        opt.value = c.id;
                        opt.textContent = `ID #${c.id} - ${c.nombre} (${c.correo})`;
                        this.dom.clienteSelect.appendChild(opt);
                    });
                    this.state.clienteId = clientes[0].id;
                    if (this.dom.clienteIdInput) this.dom.clienteIdInput.value = clientes[0].id;
                } else {
                    this.dom.clienteSelect.innerHTML = '<option value="1">Cliente Demo (ID: 1)</option>';
                }
            } else {
                this.dom.clienteSelect.innerHTML = '<option value="1">Cliente Demo (ID: 1)</option>';
            }
        } catch (err) {
            console.warn('No se pudo listar clientes desde API, usando ID 1 por defecto:', err);
            this.dom.clienteSelect.innerHTML = '<option value="1">Cliente Demo (ID: 1)</option>';
        }
    },

    // ──────────────────────────────────────────────────────────────────────────
    // PASO 1: CARGA DE SERVICIOS
    // ──────────────────────────────────────────────────────────────────────────
    async loadServicios() {
        try {
            this.dom.servicioSelect.innerHTML = '<option value="">Cargando catálogo de servicios...</option>';
            this.dom.servicioSelect.disabled = true;

            const response = await fetch(`${this.config.apiBaseUrl}/api/servicios`);
            if (!response.ok) {
                throw new Error(`Error ${response.status}: No se pudo cargar el catálogo.`);
            }

            const data = await response.json();
            this.state.servicios = data;

            this.dom.servicioSelect.innerHTML = '<option value="">-- Selecciona un servicio --</option>';
            data.forEach(servicio => {
                const opt = document.createElement('option');
                opt.value = servicio.id;
                opt.textContent = `${servicio.nombre} (${servicio.duracionMinutos} min) - $${Number(servicio.precio).toFixed(2)}`;
                this.dom.servicioSelect.appendChild(opt);
            });

            this.dom.servicioSelect.disabled = false;

            // Auto-seleccionar primer servicio si existe
            if (data.length > 0) {
                this.dom.servicioSelect.value = data[0].id;
                this.onServicioChange(data[0].id);
            }
        } catch (error) {
            console.error('Error al cargar servicios:', error);
            this.dom.servicioSelect.innerHTML = '<option value="">Error al cargar servicios</option>';
            this.showAlert('danger', 'Error de conexión: No se pudo obtener el catálogo de servicios de Spring Boot.');
        }
    },

    onServicioChange(servicioId) {
        this.hideAlerts();
        this.state.selectedServicioId = servicioId ? Number(servicioId) : null;
        this.state.selectedSlot = null;
        
        const servicio = this.state.servicios.find(s => s.id === this.state.selectedServicioId);

        if (servicio) {
            this.dom.servicePreview.classList.add('active');
            this.dom.previewDesc.textContent = servicio.descripcion || 'Sin descripción disponible.';
            this.dom.previewCategory.textContent = servicio.categoriaNombre || 'General';
            this.dom.previewDuration.textContent = `${servicio.duracionMinutos} minutos`;
            this.dom.previewPrice.textContent = `$${Number(servicio.precio).toFixed(2)}`;
        } else {
            this.dom.servicePreview.classList.remove('active');
        }

        this.updateSummary();
        this.consultarDisponibilidad();
    },

    onFechaChange(fecha) {
        this.hideAlerts();
        this.state.selectedFecha = fecha;
        this.state.selectedSlot = null;
        this.updateSummary();
        this.consultarDisponibilidad();
    },

    // ──────────────────────────────────────────────────────────────────────────
    // PASO 3: CONSULTA DE DISPONIBILIDAD Y MATRIZ DE HORARIOS
    // ──────────────────────────────────────────────────────────────────────────
    async consultarDisponibilidad() {
        if (!this.state.selectedServicioId || !this.state.selectedFecha) {
            this.renderSlotsUI([], 'Selecciona un servicio y una fecha para consultar horarios.');
            return;
        }

        try {
            this.setSlotsLoading(true);

            const url = `${this.config.apiBaseUrl}/api/citas/disponibilidad?servicioId=${this.state.selectedServicioId}&fecha=${this.state.selectedFecha}`;
            const response = await fetch(url);

            if (!response.ok) {
                const errData = await response.json().catch(() => null);
                const msg = errData?.mensaje || `Error ${response.status} al consultar disponibilidad.`;
                throw new Error(msg);
            }

            const slots = await response.json();
            this.setSlotsLoading(false);
            this.renderSlots(slots);
        } catch (error) {
            console.error('Error al consultar disponibilidad:', error);
            this.setSlotsLoading(false);
            this.renderSlotsUI([], `No se pudo obtener la disponibilidad: ${error.message}`);
        }
    },

    setSlotsLoading(isLoading) {
        this.state.isLoadingSlots = isLoading;
        if (this.dom.slotsLoading) {
            this.dom.slotsLoading.classList.toggle('d-none', !isLoading);
        }
        if (this.dom.slotsGrid) {
            this.dom.slotsGrid.classList.toggle('d-none', isLoading);
        }
        if (this.dom.slotsEmptyState) {
            this.dom.slotsEmptyState.classList.toggle('d-none', isLoading);
        }
    },

    renderSlots(slots) {
        if (!slots || slots.length === 0) {
            this.renderSlotsUI([], 'No hay horarios disponibles para la fecha seleccionada. Por favor, elige otro día.');
            return;
        }

        this.dom.slotsEmptyState.classList.add('d-none');
        this.dom.slotsGrid.classList.remove('d-none');
        this.dom.slotsGrid.innerHTML = '';

        slots.forEach(slot => {
            const btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'slot-btn available';
            
            // Formatear hora (HH:mm)
            const horaInicioFormatted = this.formatTime(slot.horaInicio);
            const horaFinFormatted = this.formatTime(slot.horaFin);

            btn.innerHTML = `
                <span class="slot-time">${horaInicioFormatted}</span>
                <span class="slot-specialist">👤 ${slot.empleadoNombre || 'Especialista'}</span>
            `;

            btn.addEventListener('click', () => {
                document.querySelectorAll('.slot-btn').forEach(b => b.classList.remove('selected'));
                btn.classList.add('selected');
                this.state.selectedSlot = slot;
                this.updateSummary();
            });

            this.dom.slotsGrid.appendChild(btn);
        });
    },

    renderSlotsUI(slots, emptyMessage) {
        this.dom.slotsGrid.classList.add('d-none');
        this.dom.slotsGrid.innerHTML = '';
        this.dom.slotsEmptyState.classList.remove('d-none');
        
        const textElem = this.dom.slotsEmptyState.querySelector('p');
        if (textElem) {
            textElem.textContent = emptyMessage;
        }
    },

    formatTime(timeStr) {
        if (!timeStr) return '';
        const parts = timeStr.split(':');
        let hours = parseInt(parts[0], 10);
        const minutes = parts[1] || '00';
        const ampm = hours >= 12 ? 'PM' : 'AM';
        hours = hours % 12;
        hours = hours ? hours : 12; // 0 debe ser 12
        return `${hours}:${minutes} ${ampm}`;
    },

    // ──────────────────────────────────────────────────────────────────────────
    // RESUMEN Y ESTADOS DEL BOTÓN
    // ──────────────────────────────────────────────────────────────────────────
    updateSummary() {
        const servicio = this.state.servicios.find(s => s.id === this.state.selectedServicioId);
        
        if (this.dom.summaryServicio) {
            this.dom.summaryServicio.textContent = servicio ? servicio.nombre : 'No seleccionado';
        }
        if (this.dom.summaryFecha) {
            this.dom.summaryFecha.textContent = this.state.selectedFecha || 'No seleccionada';
        }
        if (this.dom.summaryHora) {
            this.dom.summaryHora.textContent = this.state.selectedSlot 
                ? `${this.formatTime(this.state.selectedSlot.horaInicio)} - ${this.formatTime(this.state.selectedSlot.horaFin)}`
                : 'No seleccionada';
        }
        if (this.dom.summaryEspecialista) {
            this.dom.summaryEspecialista.textContent = this.state.selectedSlot 
                ? this.state.selectedSlot.empleadoNombre 
                : 'Automático';
        }
        if (this.dom.summaryPrecio) {
            this.dom.summaryPrecio.textContent = servicio 
                ? `$${Number(servicio.precio).toFixed(2)}` 
                : '$0.00';
        }

        // Habilitar o deshabilitar botón de reserva
        const isReady = Boolean(this.state.selectedServicioId && this.state.selectedFecha && this.state.selectedSlot);
        this.dom.btnReservar.disabled = !isReady || this.state.isSubmitting;
    },

    // ──────────────────────────────────────────────────────────────────────────
    // PASO 4: CREAR RESERVA DE CITA (POST /api/citas/reservar)
    // ──────────────────────────────────────────────────────────────────────────
    async submitReserva() {
        if (!this.state.selectedServicioId || !this.state.selectedFecha || !this.state.selectedSlot) {
            this.showAlert('danger', 'Por favor completa todos los pasos para confirmar tu cita.');
            return;
        }

        const clienteId = Number(this.dom.clienteIdInput?.value) || this.state.clienteId || 1;

        // Normalizar hora al formato HH:mm:ss esperado por Spring Boot LocalTime
        let hora = this.state.selectedSlot.horaInicio;
        if (hora.length === 5) {
            hora = `${hora}:00`;
        }

        const payload = {
            clienteId: clienteId,
            empleadoId: this.state.selectedSlot.empleadoId,
            servicioId: this.state.selectedServicioId,
            fecha: this.state.selectedFecha,
            hora: hora
        };

        try {
            this.setSubmitting(true);
            this.hideAlerts();

            const response = await fetch(`${this.config.apiBaseUrl}/api/citas/reservar`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify(payload)
            });

            const data = await response.json();

            if (response.status === 201 || response.ok) {
                // Éxito
                const successMsg = data.mensaje || 
                    `¡Cita reservada con éxito! Fecha: ${data.fecha}, Hora: ${data.hora} con ${data.empleadoNombre}.`;
                this.showAlert('success', successMsg);
                
                // Limpiar slot seleccionado y refrescar disponibilidad
                this.state.selectedSlot = null;
                this.updateSummary();
                await this.consultarDisponibilidad();
            } else if (response.status === 409) {
                // Conflicto de horario / doble reserva
                const errorMsg = data.detalles || data.mensaje || 'El horario seleccionado ya no está disponible. Por favor selecciona otra franja horaria.';
                this.showAlert('danger', `Conflicto de reserva (409): ${errorMsg}`);
                // Refrescar para remover el horario ocupado
                await this.consultarDisponibilidad();
            } else if (response.status === 400) {
                // Error de validación
                const valMsg = data.detalles || data.mensaje || 'Los datos de la reserva no cumplen con los requisitos.';
                this.showAlert('danger', `Datos inválidos: ${valMsg}`);
            } else {
                // Otro error
                const errorMsg = data.mensaje || `Error inesperado (${response.status}) al procesar la reserva.`;
                this.showAlert('danger', errorMsg);
            }
        } catch (error) {
            console.error('Error de red al reservar cita:', error);
            this.showAlert('danger', 'Error de comunicación con el servidor. Verifica que Spring Boot esté ejecutándose en http://localhost:8080.');
        } finally {
            this.setSubmitting(false);
        }
    },

    setSubmitting(isSubmitting) {
        this.state.isSubmitting = isSubmitting;
        this.dom.btnReservar.disabled = isSubmitting;
        this.dom.btnText.textContent = isSubmitting ? 'Procesando reserva...' : 'Confirmar y Reservar Cita';
        this.dom.btnSpinner.classList.toggle('d-none', !isSubmitting);
    },

    // ──────────────────────────────────────────────────────────────────────────
    // MANEJO DE ALERTAS Y FEEDBACK
    // ──────────────────────────────────────────────────────────────────────────
    showAlert(type, message) {
        this.hideAlerts();
        if (type === 'success') {
            this.dom.alertSuccessMsg.textContent = message;
            this.dom.alertSuccess.classList.remove('d-none');
            this.dom.alertSuccess.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        } else {
            this.dom.alertDangerMsg.textContent = message;
            this.dom.alertDanger.classList.remove('d-none');
            this.dom.alertDanger.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        }
    },

    hideAlerts() {
        if (this.dom.alertSuccess) this.dom.alertSuccess.classList.add('d-none');
        if (this.dom.alertDanger) this.dom.alertDanger.classList.add('d-none');
    }
};
