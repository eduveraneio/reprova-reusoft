function textboxEmpty(tbox) {
  let val = tbox.val();
  return val == '' || val == tbox.prop('name');
}

async function load() {
  const request = await fetch(
    '/api/persons?token=' + token + '&id=' + id
  );
  const person = await request.json();

  $('#name').val(person.name);
  $('#email').val(person.email);
  $('#type').val(person.type);
  $('#password').val(person.password);

  return person;
}

async function save() {
  const name = $('#name');
  const email = $('#email');
  const password = $('#password');

  if (textboxEmpty(name)) {
    alert('Please fill in the name!');
    name.focus();
    return;
  }

  if (textboxEmpty(email)) {
    alert('Please fill in the email!');
    email.focus();
    return;
  }
  
  if (textboxEmpty(password)) {
    alert('Please fill in the password!');
    password.focus();
    return;
  }

  person = {
    'name': name.val(),
    'email': email.val(),
    'password': password.val(),
    'type': '1'
  };

  if (id)
    person['id'] = id;

  const request = await fetch(
    '/api/persons?token=' + token,
    {
      method: 'post',
      body: JSON.stringify(person)
    }
  );

  if (request.ok) {
    alert('Success to upload teacher!');
    window.location = token ? '/teachers.html?token=' + token
                            : '/teachers.html';
  }
  else
    alert('Failed to upload teacher!');
}


const url = new URL(window.location.href);
const token = url.searchParams.get("token");
const id = url.searchParams.get("id");

let person = {
  record: {}
};

$(document).ready(
  async() => {
    const inputs = $('input[type=text],input[type=password]');

    inputs.focus(
      function() {
          $(this).val('');
      }
    );

    inputs.blur(
      function() {
        if ($(this).val() == '')
          $(this).val($(this).prop('name'));
      }
    );

    $('#save').click(save);

    if (id)
      person = await load(id);
  }
);
