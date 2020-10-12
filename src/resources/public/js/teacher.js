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

function personsTable(data) {
  const headers = [
    [ { text: 'Teachers', colSpan: '5' } ],
    [ 'Name', 'Email' ]
  ];
  const rows = data.map(
    q => {
      // wordwrap works better if the element is enclosed by a div.
      let name = document.createElement('div');
      name.className = 'name';
      name.appendChild(document.createTextNode(q.name));

	    return [
	      { elem: name },
	      q.email
	    ];
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

async function loadPersons() {
  const request = await fetch('/api/persons?token=' + token);
  const response = await request.json();

  let table = personsTable(response);
  table.id = 'persons';

  $('#person').append(table);

  return $('#persons').DataTable(
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

const url = new URL(window.location.href);
const token = url.searchParams.get("token");

let personsDataTable;

$(document).ready( 	
  async () => {   
  	personsDataTable = await loadPersons();  
  }
);
