/* Reprogramación de citas (SCRUM-3) */
(function () {
    var body = document.body;
    var citaId = body.getAttribute('data-cita-id');
    var clienteId = body.getAttribute('data-cliente-id');
    var servicioId = body.getAttribute('data-servicio-id');
    var csrfToken = body.getAttribute('data-csrf') || '';

    var inputFecha = document.getElementById('nuevaFecha');
    var btnConsultar = document.getElementById('btnConsultar');
    var contSlots = document.getElementById('slots');
    var inputEmpleadoId = document.getElementById('empleadoId');
    var inputHora = document.getElementById('hora');
    var resumen = document.getElementById('resumen');
    var btnReprogramar = document.getElementById('btnReprogramar');
    var alertaExito = document.getElementById('alertaExito');
    var alertaError = document.getElementById('alertaError');

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

    function actualizarResumen() {
        if (!inputHora.value || !inputEmpleadoId.value) {
            resumen.classList.add('d-none');
            btnReprogramar.disabled = true;
            return;
        }
        var slot = contSlots.querySelector('.slot.activo');
        var empleadoNombre = slot ? slot.querySelector('.slot-emp').textContent : '';
        var horaFin = slot && slot.dataset.horaFin ? slot.dataset.horaFin : '';

        resumen.innerHTML =
            '<strong>' + empleadoNombre + '</strong><br>' +
            'Nueva fecha: ' + inputFecha.value + ' &middot; Hora: ' + inputHora.value +
            (horaFin ? ' - ' + horaFin : '');
        resumen.classList.remove('d-none');
        btnReprogramar.disabled = false;
    }

    inputFecha.min = hoyISO();

    btnConsultar.addEventListener('click', function () {
        ocultarAlertas();
        inputEmpleadoId.value = '';
        inputHora.value = '';
        resumen.classList.add('d-none');
        btnReprogramar.disabled = true;

        if (!inputFecha.value) {
            mostrarAlerta(alertaError, 'Selecciona una fecha para consultar los horarios.');
            return;
        }

        contSlots.innerHTML = '<p style="color:var(--auth-text-soft);">Consultando disponibilidad...</p>';

        fetch('/api/citas/disponibilidad?servicioId=' + servicioId + '&fecha=' + inputFecha.value)
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
                    boton.innerHTML = '<span class="slot-hora">' + s.horaInicio.substring(0, 5) + '</span>' +
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

    document.getElementById('formReprogramar').addEventListener('submit', function (evento) {
        evento.preventDefault();
        ocultarAlertas();

        if (!inputFecha.value || !inputEmpleadoId.value || !inputHora.value) {
            mostrarAlerta(alertaError, 'Completa todos los pasos antes de confirmar la reprogramación.');
            return;
        }

        btnReprogramar.disabled = true;

        fetch('/api/citas/reprogramar', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            },
            body: JSON.stringify({
                citaId: Number(citaId),
                clienteId: Number(clienteId),
                fecha: inputFecha.value,
                hora: inputHora.value
            })
        })
            .then(function (r) {
                return r.json().then(function (datos) { return { ok: r.ok, datos: datos }; });
            })
            .then(function (resultado) {
                if (!resultado.ok) {
                    var detalle = resultado.datos && resultado.datos.mensaje ? resultado.datos.mensaje : 'No se pudo reprogramar la cita.';
                    mostrarAlerta(alertaError, detalle);
                    btnReprogramar.disabled = false;
                    return;
                }
                mostrarAlerta(alertaExito, resultado.datos.mensaje + ' Puedes verla en "Mis citas".');
                resumen.classList.add('d-none');
                btnReprogramar.disabled = true;
            })
            .catch(function () {
                mostrarAlerta(alertaError, 'Error de conexión al reprogramar. Intenta de nuevo.');
                btnReprogramar.disabled = false;
            });
    });
})();
