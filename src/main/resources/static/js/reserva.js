/* Reserva de citas en linea (SCRUM-1, logica de SamAlonsopp adaptada al diseno de la plataforma) */
(function () {
    var body = document.body;
    var clienteId = body.getAttribute('data-cliente-id');
    var csrfToken = body.getAttribute('data-csrf') || '';
    var servicioPreseleccionado = body.getAttribute('data-servicio-id');

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

    var servicios = [];

    function hoyISO() {
        var d = new Date();
        var m = String(d.getMonth() + 1).padStart(2, '0');
        var dia = String(d.getDate()).padStart(2, '0');
        return d.getFullYear() + '-' + m + '-' + dia;
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

            if (servicioPreseleccionado) {
                selectServicio.value = servicioPreseleccionado;
                selectServicio.dispatchEvent(new Event('change'));
            }
        })
        .catch(function () {
            selectServicio.innerHTML = '<option value="">Error al cargar los servicios</option>';
        });

    inputFecha.min = hoyISO();

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

        fetch('/api/citas/disponibilidad?servicioId=' + selectServicio.value + '&fecha=' + inputFecha.value)
            .then(function (r) { return r.json(); })
            .then(function (slots) {
                if (!slots.length) {
                    contSlots.innerHTML = '<p style="color:var(--auth-text-soft);">No hay horarios disponibles para esa fecha. Prueba con otro día.</p>';
                    return;
                }
                contSlots.innerHTML = '';
                slots.forEach(function (s) {
                    var boton = document.createElement('button');
                    boton.type = 'button';
                    boton.className = 'slot';
                    boton.dataset.empleadoId = s.empleadoId;
                    boton.dataset.hora = s.horaInicio;
                    boton.dataset.horaFin = s.horaFin;
                    boton.innerHTML = '<span class="slot-hora">' + s.horaInicio.substring(0, 5) + ' – ' + s.horaFin.substring(0, 5) + '</span>' +
                        '<span class="slot-emp">' + s.empleadoNombre + '</span>';
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
                contSlots.innerHTML = '';
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
                clienteId: Number(clienteId),
                empleadoId: Number(inputEmpleadoId.value),
                servicioId: Number(selectServicio.value),
                fecha: inputFecha.value,
                hora: inputHora.value
            })
        })
            .then(function (r) {
                return r.json().then(function (datos) { return { ok: r.ok, datos: datos }; });
            })
            .then(function (resultado) {
                if (!resultado.ok) {
                    var detalle = resultado.datos && resultado.datos.mensaje ? resultado.datos.mensaje : 'No se pudo reservar la cita.';
                    mostrarAlerta(alertaError, detalle);
                    btnReservar.disabled = false;
                    return;
                }
                mostrarAlerta(alertaExito, resultado.datos.mensaje + ' Puedes verla en "Mis citas".');
                resumen.classList.add('d-none');
                btnReservar.disabled = true;
            })
            .catch(function () {
                mostrarAlerta(alertaError, 'Error de conexión al reservar. Intenta de nuevo.');
                btnReservar.disabled = false;
            });
    });
})();
