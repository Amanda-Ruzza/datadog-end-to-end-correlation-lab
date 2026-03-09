(function () {
  document.querySelectorAll('form[data-add-to-cart]').forEach(function (form) {
    form.addEventListener('submit', function (e) {
      e.preventDefault();
      var formData = new FormData(form);
      fetch(form.action, {
        method: 'POST',
        body: formData,
        credentials: 'same-origin',
        redirect: 'follow',
      })
        .then(function () {
          window.location.href = '/cart';
        })
        .catch(function () {
          window.location.href = '/cart';
        });
    });
  });
})();
