function mean(arr) {
  if (arr.length == 0)
    return 0;

  let sum = arr.reduce((a, b) => a + b);

  return sum / arr.length;
}


function table_build_section(rows, container_elem, row_elem, cell_elem) {
  function str_or_obj(x) {
    x = typeof x === 'string' ? { name: x } : x;

    x.className = x.className || '';

    return x;
  };

  container_elem = str_or_obj(container_elem);
  row_elem = str_or_obj(row_elem);
  cell_elem = str_or_obj(cell_elem);

  let container = document.createElement(container_elem.name);
  container.className = container_elem.className;

  for (row of rows) {
    let row_container = document.createElement(row_elem.name);
    row_container.className = row_elem.className;

    for (cell of row) {
      let cell_container = document.createElement(cell_elem.name);
      cell_container.className = cell_elem.className;
      cell_container.colSpan = cell.colSpan || '1';

      if (cell.elem)
        cell_container.appendChild(cell.elem);
      else
        cell_container.appendChild(document.createTextNode(cell.text || cell));

      row_container.appendChild(cell_container);
    }

    container.appendChild(row_container);
  }

  return container;
}

function classroomsTable(data) {
  const headers = [
    [ { text: 'Classrooms', colSpan: '5' } ],
    token ? [ 'Name', 'Quantity of Students', 'Actions' ]
          : [ 'Name', 'Quantity of Students' ]
  ];
  const rows = data.map(
    q => {
      // wordwrap works better if the element is enclosed by a div.
      let name = document.createElement('div');
      name.className = 'name';
      name.appendChild(document.createTextNode(q.name));

      let actions = document.createElement('div');

      if (token) {
        let remove = document.createElement('button');
        remove.appendChild(document.createTextNode('Remove'));
        remove.type = 'button';
        remove.onclick = () => {
          if (confirm('Remove classroom?'))
            removeClassroom(q.id);
        };
        actions.appendChild(remove);

        let edit = document.createElement('button');
        edit.appendChild(document.createTextNode('Edit'));
        edit.type = 'button';
        edit.onclick = () => editClassroom(q.id);
        actions.appendChild(edit);

        return [
          { elem: name },
          "2",
          { elem: actions }
        ];
      }
      else {

        return [
          { elem: name },
          "2"
        ];
      }
    }
  );


  let table = document.createElement('table');
  table.className = 'questions-table';

  table.appendChild(
    table_build_section(
      headers,
      'thead',
      'tr',
      { name: 'th', className: 'questions-header' }
    )
  );
  table.appendChild(
    table_build_section(
      rows,
      'tbody',
      'tr',
      { name: 'td', className: 'questions-cell' }
    )
  );

  return table;
}

async function removeClassroom(id) {
  const request = await fetch(
    '/api/classrooms?token=' + token + '&id=' + id,
    { method: 'delete' }
  );

  if (request.ok)
    refresh();
  else
    alert('Failed to delete classroom!');
}

function editClassroom(id) {
  let location = '/classroom.html?id=' + id;

  if (token)
    location += '&token=' + token;

  window.location = location;
};

async function loadClassrooms() {
  const request = await fetch('/api/classrooms?token=' + token);
  const response = await request.json();

console.log(response);

  let table = classroomsTable(response);
  table.id = 'classrooms';

  $('#classroom').append(table);

  return $('#classrooms').DataTable(
    {
      'columnDefs': [
        {
          'max-width': '35%',
          'targets': 0
        }
      ],
    }
  );
}

async function refresh() {
  classroomDataTable.destroy(); // remove datatable.

  $('#classrooms').remove(); // remove table.

  classroomDataTable = await loadClassrooms(); // rebuild table.
}


const url = new URL(window.location.href);
const token = url.searchParams.get("token");

let classroomDataTable;

$(document).ready( 	
  async () => {   
  	if (token)
      $('#new-classroom').click(
        () => {
          let location = '/classroom.html';

          if (token)
            location += '?token=' + token;

          window.location = location;
        }
      );
    else
      $('#new-classroom').hide();
  
  	classroomDataTable = await loadClassrooms();
  }
);
