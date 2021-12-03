package br.com.cursojsf;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import com.google.gson.Gson;

import br.com.dao.DaoGeneric;
import br.com.entidades.Cidades;
import br.com.entidades.Estados;
import br.com.entidades.Pessoa;
import br.com.jpautil.JPAUtil;
import br.com.repository.IDaoPessoa;
import br.com.repository.IDaoPessoaImpl;

@ManagedBean(name = "pessoaBean")
@ViewScoped
public class PessoaBean {

	private Pessoa pessoa = new Pessoa();
	private DaoGeneric<Pessoa> daoGeneric = new DaoGeneric<Pessoa>();
	private List<Pessoa> pessoas = new ArrayList<Pessoa>();
	
	private IDaoPessoa iDaoPessoa = new IDaoPessoaImpl();
	
	private List<SelectItem> estados;
	
	private List<SelectItem> cidades;

	public void pesquisaCep(AjaxBehaviorEvent event) {
		
		System.out.println("Metodo pesquisa cep chamado CEP: " + pessoa.getCep());
		try {
		
			URL url = new URL("https://viacep.com.br/ws/"+pessoa.getCep()+"/json/");
			URLConnection connection = url.openConnection();
			InputStream is = connection.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String cep = "";
			StringBuilder jsonCep = new StringBuilder();
			
			while((cep = br.readLine()) != null) {
				jsonCep.append(cep);
			}
			Pessoa gsonAux = new Gson().fromJson(jsonCep.toString(), Pessoa.class);
			
			pessoa.setCep(gsonAux.getCep());
			pessoa.setLogradouro(gsonAux.getLogradouro());
			pessoa.setBairro(gsonAux.getBairro());
			pessoa.setLocalidade(gsonAux.getLocalidade());
			pessoa.setUf(gsonAux.getUf());
			pessoa.setIbge(gsonAux.getIbge());
			pessoa.setGia(gsonAux.getGia());
			
		} catch (Exception e) {
			e.printStackTrace();
			mostrarMsg("Erro ao consultar o cep");
		}
		
	}
	
	public List<Pessoa> getPessoas() {
		return pessoas;
	}

	public String salvar() {
		pessoa = daoGeneric.merge(pessoa);
		carregarPessoas();
		mostrarMsg("Cadastrado com sucesso!");
		return "";
	}

	private void mostrarMsg(String msg) {
		FacesContext context = FacesContext.getCurrentInstance();
		FacesMessage message = new FacesMessage(msg);
		context.addMessage(null, message);
	}

	public Pessoa getPessoa() {
		return pessoa;
	}

	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}

	public DaoGeneric<Pessoa> getDaoGeneric() {
		return daoGeneric;
	}

	public void setDaoGeneric(DaoGeneric<Pessoa> daoGeneric) {
		this.daoGeneric = daoGeneric;
	}

	public String novo() {
		/* Executa algum processo antes de novo*/
		pessoa = new Pessoa();
		return "";
	}
	
	public String limpar() {
		/* Executa algum processo antes de limpas*/
		pessoa = new Pessoa();
		return "";
	}

	public String remove() {
		daoGeneric.deletePorId(pessoa);
		pessoa = new Pessoa();
		carregarPessoas();
		mostrarMsg("Removido com sucesso!");
		return "";
	}

	@PostConstruct
	public void carregarPessoas() {
		pessoas = daoGeneric.getListEntity(Pessoa.class);
	}

	public String logar() {
		
		Pessoa pessoaUser = iDaoPessoa.consultarUsuario(pessoa.getLogin(), pessoa.getSenha());
		
		if(pessoaUser != null) { //achou o usuario
			
			//adiciona o usuario na sessao usuariologado
			FacesContext context = FacesContext.getCurrentInstance();
			ExternalContext externalContext = context.getExternalContext();
			externalContext.getSessionMap().put("usuariologado", pessoaUser);
			
			return "primeirapagina.jsf";
		}
		
		return "index.jsf";
	}
	
	public String deslogar() {
			//remove o usuario na sessao usuariologado
			FacesContext context = FacesContext.getCurrentInstance();
			ExternalContext externalContext = context.getExternalContext();
			externalContext.getSessionMap().remove("usuariologado");
			HttpServletRequest httpServletRequest = (HttpServletRequest) context.getCurrentInstance().getExternalContext().getRequest();
			httpServletRequest.getSession().invalidate();
			return "index.jsf";
	}
	
	public boolean permiteAcesso(String acesso) {
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();
		Pessoa pessoaUser = (Pessoa) externalContext.getSessionMap().get("usuariologado");
		return pessoaUser.getPerfilUser().equals(acesso);
	}

	public List<SelectItem> getEstados() {
		estados = iDaoPessoa.litaEstados();
		return estados;
	}
	
	public void carregaCidades(AjaxBehaviorEvent event) {
		
		String codigoEstado = (String) event.getComponent().getAttributes().get("submittedValue");
		
		if(codigoEstado != null) {
			System.out.println(codigoEstado);
			Estados estado = JPAUtil.getEntityManager().find(Estados.class, Long.parseLong(codigoEstado));
		
			if(estado != null) {
				pessoa.setEstados(estado);
				
				List<Cidades> cidades = JPAUtil.getEntityManager().createQuery("from Cidades where estados.id = " + codigoEstado).getResultList();
			
				List<SelectItem> selectItemsCidade = new ArrayList<SelectItem>();
				
				for (Cidades cidade : cidades) {
					selectItemsCidade.add(new SelectItem(cidade.getId(), cidade.getNome()));
					
				}
				setCidades(selectItemsCidade);
			
			}
		}
	}

	public List<SelectItem> getCidades() {
		return cidades;
	}

	public void setCidades(List<SelectItem> cidades) {
		this.cidades = cidades;
	}
	
	
	
}
