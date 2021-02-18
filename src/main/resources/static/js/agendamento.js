//busca as especialidades com auto-complete
$("#especialidade").autocomplete({				//especialidade é o campo de input
    source: function (request, response) {
        $.ajax({
            method: "GET",
            url: "/especialidades/titulo",
            data: {
        	//Pega o valor do campo, coloca na variável termo e envia para o controller
            	termo: request.term
			},
            success: function (data) {
            	response(data);
            }
        });
    }
});

/*Após a especialidade ser selecionado busca os médicos referentes e os adiciona na página com radio
blur é uma função qnd tira o foco do componente, qnd tirar o foco de "especialidade" chama essa função*/
$('#especialidade').on('blur', function() {				
    $('div').remove(".custom-radio");			//Se mudar de especialidade, remove o radio com a lista de médicos para atualizar
    
	var titulo = $(this).val();					//Pega o valor selecionado
	
	if ( titulo != '' ) {						//Verifica se tem alguma funcionalidade selecionada no input
		$.get( "/medicos/especialidade/titulo/" + titulo , function(result) {		//Retorna uma lista de médicos
				
			var ultimo = result.length - 1; 		//Se tem 5 médicos, então a pos precisa ir de 0 até 4	
			
			$.each(result, function (k, v) {
				
				if ( k == ultimo ) {				//No exemplo, o valor de ultimo é a pos 4
	//Cada médico será um radio. Depois do ultimo médico mostrado, aparece msg vermelha de escolha obrigatória
	    			$("#medicos").append( 
	    				 '<div class="custom-control custom-radio">'	
	    				+  '<input class="custom-control-input" type="radio" id="customRadio'+ k +'" name="medico.id" value="'+ v.id +'" required>'
						+  '<label class="custom-control-label" for="customRadio'+ k +'">'+ v.nome +'</label>'
						+  '<div class="invalid-feedback">Médico é obrigatório</div>'
						+'</div>'
	    			);
				} else {						//Os divs são adicionados onde possui o id "medico"
	    			$("#medicos").append( 
	    				 '<div class="custom-control custom-radio">'	
	    				+  '<input class="custom-control-input" type="radio" id="customRadio'+ k +'" name="medico.id" value="'+ v.id +'" required>'
						+  '<label class="custom-control-label" for="customRadio'+ k +'">'+ v.nome +'</label>'
						+'</div>'
	        		);	            				
				}
		    });
		});
	}
});	

/** 
 * busca os horários livres para consulta conforme a data e o médico.
 * os horários são adicionados a página como um select:option.	
*/
$('#data').on('blur', function () {
	$("#horarios").empty();						//"horarios" é um select (comboBox)
    var data = $(this).val();					//Pega a data selecionada no componente de data
    /*Recupera o id do médico escolhido. O checked significa q o médico precisa está selecionado
    O name="medico.id" está na tag adicionada pela função JS*/
    var medico = $('input[name="medico.id"]:checked').val();		
    
    if (Date.parse(data)) {					//Se tiver uma data selecionada retorna true, se n, retorna false
        $.get('/agendamentos/horario/medico/'+ medico + '/data/' + data , function( result ) {
        	
        	//A instrução acima retorna uma lista de horários 
        	$.each(result, function (k, v) {
                $("#horarios").append( 
                    '<option class="op" value="'+ v.id +'">'+ v.horaMinuto + '</option>'
                );	            			
            });
        });
    }
});

//Datatable histórico de consultas
$(document).ready(function() {
    moment.locale('pt-BR');
    var table = $('#table-paciente-historico').DataTable({
        searching : false,
        lengthMenu : [ 5, 10 ],
        processing : true,
        serverSide : true,
        responsive : true,
        order: [2, 'desc'],						//A tabela está sendo ordenada pela coluna n°2
        ajax : {
            url : '/agendamentos/datatables/server/historico',
            data : 'data'
        },
        columns : [
            {data : 'id'},
            {data : 'paciente.nome'},
            {data: 'dataConsulta', render:
                function( dataConsulta ) {
                    return moment(dataConsulta).format('LLL');				//Formatação da data
                }
            },
            {data : 'medico.nome'},
            {data : 'especialidade.titulo'},
            //Colunas referentes aos botões com links de editar e excluir
            {orderable : false,	data : 'id', "render" : function(id) {
                    return '<a class="btn btn-success btn-sm btn-block" href="/agendamentos/editar/consulta/'
                            + id + '" role="button"><i class="fas fa-edit"></i></a>';
                }
            },
            {orderable : false,	data : 'id', "render" : function(id) {
                    return '<a class="btn btn-danger btn-sm btn-block" href="/agendamentos/excluir/consulta/'
                    + id +'" role="button" data-toggle="modal" data-target="#confirm-modal"><i class="fas fa-times-circle"></i></a>';
                }
            }
        ]
    });
});


















