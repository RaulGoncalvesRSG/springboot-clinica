//datatables - lista de médicos
$(document).ready(function() {
	moment.locale('pt-BR');
	var table = $('#table-usuarios').DataTable({			//table Recebe o obj DataTable
		searching : true,
		lengthMenu : [ 5, 10 ],
		processing : true,
		serverSide : true,
		responsive : true,
		ajax : {
			url : '/u/datatables/server/usuarios',
			data : 'data'
		},
		columns : [
				{data : 'id'},
				{data : 'email'},
				//No BD o 'ativo' é 0 ou 1 (false or true). Modifica a forma q será apresentado na tabela
				{	data : 'ativo', 
					render : function(ativo) {
						return ativo == true ? 'Sim' : 'Não';
					}
				},
				{	data : 'perfis',									 
					render : function(perfis) {
						var aux = new Array();
						//Pega a descrição do perfil com value.desc
						$.each(perfis, function( index, value ) {
							  aux.push(value.desc);
						});
						return aux;		//Retorna a lista de perfis do usuário, no qual contém a descrição
					},
					orderable : false,					//N está trabalhando com ordenação nessa coluna
				},
				{	data : 'id',						//Altera as credenciais (email, senha e perfil)
					render : function(id) {
						return ''.concat('<a class="btn btn-success btn-sm btn-block"', ' ')
								 .concat('href="').concat('/u/editar/credenciais/usuario/').concat(id, '"', ' ') 
								 .concat('role="button" title="Editar" data-toggle="tooltip" data-placement="right">', ' ')
								 .concat('<i class="fas fa-edit"></i></a>');
					},
					orderable : false
				},
				{	data : 'id',						//Altera os dados pessoais
					render : function(id) {
						return ''.concat('<a class="btn btn-info btn-sm btn-block"', ' ') 
								 .concat('id="dp_').concat(id).concat('"', ' ') 
								 .concat('role="button" title="Editar" data-toggle="tooltip" data-placement="right">', ' ')
								 .concat('<i class="fas fa-edit"></i></a>');
					},
					orderable : false
				}
		]
	});
	
	//Espera um click no btn de dados pessoais. Espera um click de um id com inicial "dp_"
    $('#table-usuarios tbody').on('click', '[id*="dp_"]', function () {
    	//$(this) do jQuery é a instrução acima retorna todos os dados da linha
    	var data = table.row($(this).parents('tr')).data();
    	var aux = new Array();
    	
		$.each(data.perfis, function( index, value ) {
			  aux.push(value.id);		//Add o id de cara obj Perfil q tem na lista de perfis
		});
		//data é a lista de colunas e está pegando o valor da coluna ID
		document.location.href = '/u/editar/dados/usuario/' + data.id + '/perfis/' + aux;
    } );	
});	

/*keyup ativia a função qnd solta a tecla. '.pass' reconhece qnd está clicando no teclado. Essa classe foi
adicionado nos campos de senha1 e senha2, então a função serve para os dois campos de input*/
$('.pass').keyup(function(){					
	$('#senha1').val() === $('#senha2').val()
	    ? $('#senha3').removeAttr('readonly')		//Se os valores forem iguais, remove o readonly
	    : $('#senha3').attr('readonly', 'readonly');			
});