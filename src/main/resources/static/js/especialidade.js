$(document).ready(function () {
	moment.locale('pt-BR');
    var table = $('#table-especializacao').DataTable({
    	searching: true,
    	order: [[ 1, "asc" ]],			//Força a ordenação pela coluna n° 1 da tabela
    	lengthMenu: [5, 10],
        processing: true,
        serverSide: true,
        responsive: true,
        ajax: {
            url: '/especialidades/datatables/server',
            data: 'data'
        },
        columns: [
            {data: 'id'},				//Coluna 0
            {data: 'titulo'},			//Coluna 1	
            {orderable: false, 			//Remove a função de ordenação do btn editar pq n vai ordenar a tabela por esse btn
             data: 'id',
                "render": function(id) {
                    return '<a class="btn btn-success btn-sm btn-block" href="/especialidades/editar/'+ 
                    	id +'" role="button"><i class="fas fa-edit"></i></a>';
                }
            //O render está criando um btn q vai ser incluído na coluna e esse btn vai ter uma url com o ID
            },
            {orderable: false,
             data: 'id',
                "render": function(id) {
                    return '<a class="btn btn-danger btn-sm btn-block" href="/especialidades/excluir/'+ 
                    	id +'" role="button" data-toggle="modal" data-target="#confirm-modal"><i class="fas fa-times-circle"></i></a>';
                }               
            }
        ]
    });
});    
