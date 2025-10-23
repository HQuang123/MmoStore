document.addEventListener('DOMContentLoaded', function () {
    const tokenInput = document.getElementById('token');
    const feedback = document.getElementById('tokenFeedback');
    const email = document.getElementById('emailHidden').value;

    function verifyToken(token) {
        fetch('/api/verify-token?email=' + encodeURIComponent(email) + '&token=' + encodeURIComponent(token))
            .then(res => res.json())
            .then(data => {
                if (data.valid) {
                    feedback.innerHTML = '<span class="text-success">Token hợp lệ!</span>';
                } else {
                    feedback.innerHTML = '<span class="text-danger">Token không hợp lệ!</span>';
                }
            })
            .catch(() => {
                feedback.innerHTML = '<span class="text-danger">Có lỗi khi kiểm tra token</span>';
            });
    }

    console.log("JS loaded - tokenInput found:", tokenInput);
    tokenInput.addEventListener('input', function () {
        const token = tokenInput.value.trim();
        if (token.length === 5) {
            verifyToken(token);
        } else {
            feedback.innerHTML = '';
        }
    });
});
