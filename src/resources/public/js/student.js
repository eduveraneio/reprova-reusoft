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
  $('#regNumber').val(person.regNumber);
  $('#password').val(person.password);

  return person;
}

async function save() {
  const name = $('#name');
  const email = $('#email');
  const regNumber = $('#regNumber');
  const password = $('#password');

  if (textboxEmpty(name)) {
    alert('Please fill in the name!');
    name.focus();
    return;
  }

  if (textboxEmpty(regNumber)) {
    alert('Please fill in the registry number!');
    regNumber.focus();
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
    'regNumber': regNumber.val(),
    'password': password.val(),
    'type': '2'
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
    alert('Success to upload student!');
    window.location = token ? '/students.html?token=' + token
                            : '/students.html';
  }
  else
    alert('Failed to upload student!');
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
