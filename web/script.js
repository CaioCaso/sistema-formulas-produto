const api = p => '/api' + p;

const el = id => document.getElementById(id);

async function showMsg(targetId, text, timeout=3000) {
  const m = el(targetId);
  m.textContent = text;
  m.classList.remove('hidden');
  setTimeout(()=> m.classList.add('hidden'), timeout);
}

/* preencher lista de produtos (datalist + select) */
async function atualizarLista(){
  try{
    const res = await fetch(api('/produtos'));
    const lista = await res.json();
    const sel = el('selectProdutos');
    sel.innerHTML = '';
    // datalist para sugestao no input de produto
    const dt = el('produtosDatalist');
    dt.innerHTML = '';
    lista.forEach(p => {
      const opt = document.createElement('option'); opt.value = p; dt.appendChild(opt);
      const o = document.createElement('option'); o.value = p; o.textContent = p; sel.appendChild(o);
    });
    // atualizar paiSelect com componentes do produto selecionado (se houver)
    const current = el('produtoInput').value || (lista.length?lista[0]:'');
    await atualizarPaiSelect(current);
  }catch(err){
    console.error('erro listar produtos', err);
  }
}

/* preenche select de pais (componentes existentes) para o produto */
async function atualizarPaiSelect(produto){
  const pai = el('paiSelect');
  pai.innerHTML = '<option value="">-- nenhum (raiz) --</option>';
  if(!produto) return;
  try{
    const res = await fetch(api('/carregar?produto=' + encodeURIComponent(produto)));
    const itens = await res.json();
    // itens é array de objetos {nome, pai, quantidade, valor}
    itens.forEach(it => {
      const o = document.createElement('option');
      o.value = it.nome; o.textContent = it.nome;
      pai.appendChild(o);
    });
  }catch(e){
    console.error('erro carregar componentes', e);
  }
}

/* comportamento ao digitar/selecionar produto: atualiza paiSelect automaticamente */
el('produtoInput').addEventListener('input', async (e) => {
  await atualizarPaiSelect(e.target.value);
});

/* formulario adicionar componente */
el('formAdd').addEventListener('submit', async (e) => {
  e.preventDefault();
  const f = new FormData(e.target);
  const produto = f.get('produto').trim();
  const nome = f.get('nome').trim();
  const pai = f.get('pai') || '';
  const quantidade = Number(f.get('quantidade') || 1);
  let valor = Number(f.get('valor') || 0);

  if(!produto || !nome){ showMsg('msgAdd','Preencha produto e nome'); return; }
  // se pai não vazio e pai é igual ao nome -> inválido
  if(pai && pai === nome){ showMsg('msgAdd','Componente não pode ser pai de si mesmo'); return; }

  // checar se estamos definindo valor para um componente que já é pai (backend também valida)
  try{
    const chk = await fetch(api('/hasChildren?produto=' + encodeURIComponent(produto) + '&nome=' + encodeURIComponent(nome)));
    const obj = await chk.json();
    if(obj.hasChildren){
      // se já é pai, forçar valor 0 e avisar
      valor = 0;
      showMsg('msgAdd','Atenção: componente já é pai de outros. Valor definido como 0.');
    }
  }catch(err){
    console.warn('checagem children falhou', err);
  }

  const body = new URLSearchParams({ produto, nome, pai, quantidade: String(quantidade), valor: String(valor) });
  try{
    const r = await fetch(api('/add'), { method:'POST', body });
    const j = await r.json();
    if(j && j.status === 'ok'){
      showMsg('msgAdd','Componente salvo');
      await atualizarLista();
      e.target.reset();
      // manter produto no input
      el('produtoInput').value = produto;
      await atualizarPaiSelect(produto);
    } else {
      showMsg('msgAdd','Erro ao salvar: ' + (j && j.error? j.error : 'unknown'));
    }
  }catch(err){
    console.error(err);
    showMsg('msgAdd','Erro ao enviar');
  }
});

/* limpar form */
el('btnLimpar').addEventListener('click', ()=> el('formAdd').reset());

el('btnListar').addEventListener('click', atualizarLista);

el('btnExplodir').addEventListener('click', async () => {
  const produto = el('selectProdutos').value;
  if(!produto){ alert('Selecione um produto'); return; }
  try{
    const res = await fetch(api('/explodir?produto=' + encodeURIComponent(produto)));
    const data = await res.json();
    el('areaExplosao').textContent = data.text || '(vazio)';
  }catch(err){
    console.error(err);
    el('areaExplosao').textContent = 'Erro ao buscar explosão';
  }
});

/* exportar TXT */
el('btnExportTxt').addEventListener('click', () => {
  const txt = el('areaExplosao').textContent || '';
  if(!txt) return alert('Nada para exportar');
  const blob = new Blob([txt], {type:'text/plain'});
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url; a.download = 'explosao.txt'; a.click();
  URL.revokeObjectURL(url);
});

/* calcular custo */
el('btnCalcular').addEventListener('click', async () => {
  const produto = el('selectProdutos').value;
  const qtd = Number(el('qtdProd').value || 1);
  if(!produto){ alert('Selecione um produto'); return; }
  try{
    const res = await fetch(api('/calcular?produto=' + encodeURIComponent(produto) + '&qtd=' + qtd));
    const data = await res.json();
    let html = `<p><b>Custo unitário:</b> R$ ${Number(data.total_unitario).toFixed(2)}</p>`;
    html += `<p><b>Total para ${qtd} unidade(s):</b> R$ ${Number(data.total_para_qtd).toFixed(2)}</p>`;
    html += '<h4>Componentes (quantidades totais)</h4><ul>';
    for(let k in data.componentes) html += `<li>${k}: ${data.componentes[k]}</li>`;
    html += '</ul>';
    el('resultadoCalculo').innerHTML = html;
  }catch(err){
    console.error(err);
    el('resultadoCalculo').textContent = 'Erro ao calcular';
  }
});

/* atualizar valor */
el('btnAtualizarValor').addEventListener('click', async () => {
  const nome = el('nomeComponente').value.trim();
  const valor = el('novoValor').value;
  const produto = el('selectProdutos').value;
  if(!produto || !nome || !valor) return alert('Preencha todos os campos');
  const body = new URLSearchParams({ nome, valor, produto });
  try{
    const res = await fetch(api('/atualizar'), { method:'POST', body });
    const j = await res.json();
    if(j && j.status === 'ok'){ alert('Valor atualizado'); await atualizarLista(); }
    else alert('Erro ao atualizar');
  }catch(err){ console.error(err); alert('Erro ao atualizar'); }
});

/* deletar produto */
el('btnExcluir').addEventListener('click', async () => {
  const produto = el('selectProdutos').value;
  if(!produto) return alert('Selecione um produto');
  if(!confirm('Excluir produto ' + produto + ' (todos os componentes)?')) return;
  const body = new URLSearchParams({ produto });
  try{
    const res = await fetch(api('/deletar'), { method:'POST', body });
    const j = await res.json();
    if(j && j.status === 'ok'){ alert('Produto excluído'); atualizarLista(); }
  }catch(err){ console.error(err); }
});

/* quando carregar a página, atualiza lista */
window.onload = atualizarLista;