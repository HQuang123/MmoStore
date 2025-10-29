document.addEventListener('DOMContentLoaded', function () {
    var form = document.getElementById('resetForm');
    var passwordInput = form.querySelector('input[name="password"]');
    var confirmInput = form.querySelector('input[name="confirmPassword"]');
    var help = document.getElementById('confirmPasswordHelp');
    var submitBtn = document.getElementById('submitBtn');

    function validatePasswords() {
        var pass = passwordInput.value;
        var confirm = confirmInput.value;
        var matched = pass.length > 0 && pass === confirm;

        if (matched) {
            confirmInput.setCustomValidity('');
            help.style.display = 'none';
        } else {
            confirmInput.setCustomValidity('Passwords do not match');
            help.style.display = confirm.length ? 'block' : 'none';
        }
        submitBtn.disabled = !matched;
    }

    passwordInput.addEventListener('input', validatePasswords);
    confirmInput.addEventListener('input', validatePasswords);
    validatePasswords(); // Initial check
});