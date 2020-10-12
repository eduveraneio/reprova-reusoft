function textboxEmpty(tbox) {
  let val = tbox.val();
  return val == '' || val == tbox.prop('name');
}

async function load() {
  const request = await fetch(
    '/api/classrooms?token=' + token + '&id=' + id
  );
  const classroom = await request.json();

  $('#name').val(classroom.name);
  
  return classroom;
}


async function save() {
  const name = $('#name');
  const total = $('#total');
  
  if (textboxEmpty(name)) {
    alert('Please fill in the name!');
    name.focus();
    return;
  }

  classroom = {
   'name': name.val(),
  };

  if (id)
    classroom['id'] = id;

  const request = await fetch(
    '/api/classrooms?token=' + token,
    {
      method: 'post',
      body: JSON.stringify(classroom)
    }
  );

  if (request.ok) {
    alert('Success to upload classroom!');
    window.location = token ? '/classrooms.html?token=' + token
                            : '/classrooms.html';
  }
  else
    alert('Failed to upload classroom!');
}


const url = new URL(window.location.href);
const token = url.searchParams.get("token");
const id = url.searchParams.get("id");

let classroom = {
  record: {}
};

$(document).ready(
  async() => {
    const inputs = $('input[type=text]');

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
      classroom = await load(id);
  }
);