/* Reserva de citas en linea (SCRUM-1, logica de SamAlonsopp adaptada al diseno de la plataforma) */
(function () {
    var body = document.body;
    var csrfToken = body.getAttribute('data-csrf') || '';

    var selectServicio = document.getElementById('servicio');
    var inputFecha = document.getElementById('fecha');
    var btnConsultar = document.getElementById('btnConsultar');
    var contSlots = document.getElementById('slots');
    var inputEmpleadoId = document.getElementById('empleadoId');
    var inputHora = document.getElementById('hora');
    var resumen = document.getElementById('resumen');
    var btnReservar = document.getElementById('btnReservar');
    var alertaExito = document.getElementById('alertaExito');
    var alertaError = document.getElementById('alertaError');
    var confirmacion = document.getElementById('confirmacionCita');
    var confirmacionId = document.getElementById('confirmacionId');
    var confirmacionFecha = document.getElementById('confirmacionFecha');
    var confirmacionHora = document.getElementById('confirmacionHora');
    var confirmacionServicio = document.getElementById('confirmacionServicio');
    var confirmacionEstado = document.getElementById('confirmacionEstado');
    var confirmacionAccionPrincipal = document.getElementById('confirmacionAccionPrincipal');

    var servicios = [];

    function hoyISO() {
        var d = new Date();
        return fechaISO(d);
    }

    function fechaISO(fecha) {
        var d = new Date(fecha.getTime());
        var m = String(d.getMonth() + 1).padStart(2, '0');
        var dia = String(d.getDate()).padStart(2, '0');
        return d.getFullYear() + '-' + m + '-' + dia;
    }

    function sumarDiasISO(cantidad) {
        var fecha = new Date();
        fecha.setDate(fecha.getDate() + cantidad);
        return fechaISO(fecha);
    }

    function formatearHoraNormal(hora) {
        var partes = hora.substring(0, 5).split(':');
        var horas = Number(partes[0]);
        var minutos = partes[1];
        var periodo = horas >= 12 ? 'p. m.' : 'a. m.';
        var horas12 = horas % 12 || 12;
        return horas12 + ':' + minutos + ' ' + periodo;
    }

    function mostrarAlerta(el, mensaje) {
        el.textContent = mensaje;
        el.classList.remove('d-none');
    }

    function ocultarAlertas() {
        alertaExito.classList.add('d-none');
        alertaError.classList.add('d-none');
    }

    function servicioSeleccionado() {
        return servicios.find(function (s) { return String(s.id) === selectServicio.value; });
    }

    function formatearFecha(fechaISO) {
        var partes = fechaISO.split('-');
        if (partes.length !== 3) return fechaISO;
        return partes[2] + '/' + partes[1] + '/' + partes[0];
    }

    function formatearHora(hora) {
        return hora && hora.length >= 5 ? hora.substring(0, 5) : hora;
    }

    function respuestaConfirmacionValida(datos) {
        return datos && datos.id != null && datos.fecha && datos.hora
            && datos.servicioNombre && datos.estado;
    }

    function mostrarConfirmacion(datos) {
        confirmacionId.textContent = '#' + datos.id;
        confirmacionFecha.textContent = formatearFecha(datos.fecha);
        confirmacionHora.textContent = formatearHora(datos.hora);
        confirmacionServicio.textContent = datos.servicioNombre;
        confirmacionEstado.textContent = datos.estado;
        confirmacionEstado.className = 'badge-soft ' + String(datos.estado).toLowerCase();
        confirmacion.classList.remove('d-none');
        confirmacion.setAttribute('aria-hidden', 'false');
        document.body.classList.add('confirmation-open');
        confirmacionAccionPrincipal.focus();
    }

    function leerRespuesta(respuesta) {
        return respuesta.text().then(function (texto) {
            var datos = {};
            if (texto) {
                try { datos = JSON.parse(texto); } catch (error) { datos = {}; }
            }
            return { ok: respuesta.ok, datos: datos };
        });
    }

    function actualizarResumen() {
        var servicio = servicioSeleccionado();
        if (!inputHora.value || !servicio) {
            resumen.classList.add('d-none');
            btnReservar.disabled = true;
            return;
        }
        var horaFin = '';
        var slot = contSlots.querySelector('.slot.activo');
        if (slot && slot.dataset.horaFin) horaFin = slot.dataset.horaFin;

        resumen.innerHTML =
            '<strong>' + servicio.nombre + '</strong><br>' +
            'Fecha: ' + inputFecha.value + ' &middot; Hora: ' + inputHora.value +
            (horaFin ? ' - ' + horaFin : '') + '<br>' +
            'Duración: ' + servicio.duracionMinutos + ' min';
        resumen.classList.remove('d-none');
        btnReservar.disabled = false;
    }

    fetch('/api/servicios')
        .then(function (r) { return r.json(); })
        .then(function (data) {
            servicios = data || [];
            if (!servicios.length) {
                selectServicio.innerHTML = '<option value="">No hay servicios disponibles</option>';
                return;
            }
            var opciones = '<option value="">Selecciona un servicio</option>';
            servicios.forEach(function (s) {
                opciones += '<option value="' + s.id + '">' + s.nombre + ' (' + s.duracionMinutos + ' min)</option>';
            });
            selectServicio.innerHTML = opciones;
        })
        .catch(function () {
            selectServicio.innerHTML = '<option value="">Error al cargar los servicios</option>';
        });

    inputFecha.min = hoyISO();
    inputFecha.max = sumarDiasISO(60);
    if (!inputFecha.value) inputFecha.value = sumarDiasISO(1);

    btnConsultar.addEventListener('click', function () {
        ocultarAlertas();
        inputEmpleadoId.value = '';
        inputHora.value = '';
        resumen.classList.add('d-none');
        btnReservar.disabled = true;

        if (!selectServicio.value) {
            mostrarAlerta(alertaError, 'Selecciona primero un servicio.');
            return;
        }
        if (!inputFecha.value) {
            mostrarAlerta(alertaError, 'Selecciona una fecha para consultar los horarios.');
            return;
        }

        contSlots.innerHTML = '<p style="color:var(--auth-text-soft);">Consultando disponibilidad...</p>';

        fetch('/api/citas/disponibilidad?servicioId=' + encodeURIComponent(selectServicio.value)
            + '&fecha=' + encodeURIComponent(inputFecha.value), {
            cache: 'no-store',
            headers: { 'Accept': 'application/json' }
        })
            .then(function (r) {
                if (!r.ok) throw new Error('No se pudo consultar la disponibilidad.');
                return r.json();
            })
            .then(function (slots) {
                if (!Array.isArray(slots)) throw new Error('Respuesta de horarios inválida.');
                if (!slots.length) {
                    contSlots.innerHTML = '<p style="color:var(--auth-text-soft);">No hay horarios disponibles para esa fecha. Prueba con otro día.</p>';
                    return;
                }
                contSlots.innerHTML = '';
                var resultado = document.createElement('p');
                resultado.className = 'slots-result-meta';
                resultado.textContent = slots.length + (slots.length === 1 ? ' horario disponible' : ' horarios disponibles');
                contSlots.appendChild(resultado);

                slots.forEach(function (s) {
                    var boton = document.createElement('button');
                    boton.type = 'button';
                    boton.className = 'slot';
                    boton.dataset.empleadoId = s.empleadoId;
                    boton.dataset.hora = s.horaInicio;
                    boton.dataset.horaFin = s.horaFin;
                    var hora = document.createElement('span');
                    hora.className = 'slot-hora';
                    hora.textContent = formatearHoraNormal(s.horaInicio);
                    var empleado = document.createElement('span');
                    empleado.className = 'slot-emp';
                    empleado.textContent = s.empleadoNombre;
                    boton.appendChild(hora);
                    boton.appendChild(empleado);
                    boton.addEventListener('click', function () {
                        contSlots.querySelectorAll('.slot').forEach(function (x) { x.classList.remove('activo'); });
                        boton.classList.add('activo');
                        inputEmpleadoId.value = s.empleadoId;
                        inputHora.value = s.horaInicio;
                        actualizarResumen();
                    });
                    contSlots.appendChild(boton);
                });
            })
            .catch(function () {
                contSlots.innerHTML = '<p style="color:var(--auth-alert-danger-text);">No se pudieron cargar los horarios. Intenta nuevamente.</p>';
                mostrarAlerta(alertaError, 'No se pudo consultar la disponibilidad. Intenta de nuevo.');
            });
    });

    document.getElementById('formReserva').addEventListener('submit', function (evento) {
        evento.preventDefault();
        ocultarAlertas();

        if (!selectServicio.value || !inputFecha.value || !inputEmpleadoId.value || !inputHora.value) {
            mostrarAlerta(alertaError, 'Completa todos los pasos antes de confirmar la reserva.');
            return;
        }

        btnReservar.disabled = true;

        fetch('/api/citas/reservar', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            },
            body: JSON.stringify({
                empleadoId: Number(inputEmpleadoId.value),
                servicioId: Number(selectServicio.value),
                fecha: inputFecha.value,
                hora: inputHora.value
            })
        })
            .then(leerRespuesta)
            .then(function (resultado) {
                if (!resultado.ok) {
                    var detalle = resultado.datos && resultado.datos.mensaje ? resultado.datos.mensaje : 'No se pudo reservar la cita.';
                    mostrarAlerta(alertaError, detalle);
                    btnReservar.disabled = false;
                    return;
                }

                if (!respuestaConfirmacionValida(resultado.datos)) {
                    mostrarAlerta(alertaError, 'La cita se registró, pero la respuesta de confirmación está incompleta. Consulta "Mis citas".');
                    btnReservar.disabled = false;
                    return;
                }

                resumen.classList.add('d-none');
                btnReservar.disabled = true;
                mostrarConfirmacion(resultado.datos);
            })
            .catch(function () {
                mostrarAlerta(alertaError, 'Error de conexión al reservar. Intenta de nuevo.');
                btnReservar.disabled = false;
            });
    });
})();
