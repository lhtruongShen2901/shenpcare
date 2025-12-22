// add-customer.js - quản lý pet list và PIN (demo lưu localStorage)
(function(){
  const petList = document.getElementById('pet-list');
  const petTemplate = document.getElementById('pet-template');
  const addPetBtn = document.getElementById('add-pet');
  const pinModal = document.getElementById('pin-modal');
  const pinSettingsModal = document.getElementById('pin-settings-modal');
  const pinConfirmBtn = document.getElementById('pin-confirm');
  const pinCancelBtn = document.getElementById('pin-cancel');

  function createPetItem(index = null) {
    const node = petTemplate.content.cloneNode(true);
    const wrapper = node.querySelector('.pet-card');

    // prepend title
    const titleEl = document.createElement('div');
    titleEl.className = 'text-xs font-semibold text-gray-500 mb-2';
    titleEl.textContent = index ? `Thú cưng #${index}` : '';
    wrapper.prepend(titleEl);

    const fileInput = wrapper.querySelector('.file-input');
    const previewImg = wrapper.querySelector('.preview-img');
    fileInput.addEventListener('change', (e) => {
      const f = e.target.files[0];
      if (!f) return;
      const url = URL.createObjectURL(f);
      previewImg.src = url; previewImg.classList.remove('hidden');
    });

    const removeBtn = wrapper.querySelector('.remove-pet');
    removeBtn.addEventListener('click', () => {
      openPinModal(() => { wrapper.remove(); closePinModal(); });
    });

    return wrapper;
  }

  addPetBtn.addEventListener('click', () => {
    const idx = petList.children.length + 1;
    const item = createPetItem(idx);
    petList.appendChild(item);
  });

  // init one pet
  addPetBtn.click();

  // Sử dụng modal xác thực mã PIN chuẩn
  // Đảm bảo fragment pin_modal.html đã được nhúng vào trang
  // Để xác thực mã PIN, chỉ cần gọi openPinModal(callback)
  // Callback sẽ được gọi nếu xác thực thành công

  // pin settings
  const openPinSettingsBtn = document.getElementById('open-pin-settings');
  if(openPinSettingsBtn){
    openPinSettingsBtn.addEventListener('click', ()=>{
      if(pinSettingsModal) pinSettingsModal.classList.remove('hidden');
      setTimeout(()=>{ const sp = document.getElementById('set-pin-1'); if(sp) sp.focus(); },100);
    });
  }
  const setPinCancel = document.getElementById('set-pin-cancel');
  if(setPinCancel) setPinCancel.addEventListener('click', ()=> pinSettingsModal.classList.add('hidden'));
  const setPinSave = document.getElementById('set-pin-save');
  if(setPinSave) setPinSave.addEventListener('click', async ()=>{
    const p1 = document.getElementById('set-pin-1').value||'';
    const p2 = document.getElementById('set-pin-2').value||'';
    const p3 = document.getElementById('set-pin-3').value||'';
    const p4 = document.getElementById('set-pin-4').value||'';
    const pin = p1+p2+p3+p4;
    if(!(pin.length===4 && /^[0-9]{4}$/.test(pin))) { alert('Vui lòng nhập 4 chữ số hợp lệ.'); return; }

    try {
      const res = await fetch('/api/pin/set', { method: 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify({ pin }) });
      if(!res.ok){ const txt = await res.text(); alert('Không thể lưu PIN: '+txt); return; }
      const msg = document.getElementById('set-pin-msg'); if(msg) msg.classList.remove('hidden');
      setTimeout(()=>{ if(msg) msg.classList.add('hidden'); pinSettingsModal.classList.add('hidden'); document.getElementById('set-pin-1').value=''; document.getElementById('set-pin-2').value=''; document.getElementById('set-pin-3').value=''; document.getElementById('set-pin-4').value=''; },1000);
    } catch(err){ console.error(err); alert('Lỗi khi lưu PIN'); }
  });

  // Save action (demo)
  const saveBtn = document.getElementById('save-all');
  if(saveBtn) saveBtn.addEventListener('click', async ()=>{
    const customer = {
      name: document.getElementById('customer-name').value,
      phone: document.getElementById('phone').value,
      email: document.getElementById('email').value,
      address: document.getElementById('address').value
    };
    const pets = [];
    petList.querySelectorAll('.pet-card').forEach(card=>{
      const name = card.querySelector('.pet-name')?.value || '';
      if(!name) return;
      const species = card.querySelector('.pet-species')?.value || '';
      const breed = card.querySelector('.pet-breed')?.value || '';
      const dob = card.querySelector('.pet-dob')?.value || '';
      const notes = card.querySelector('.pet-notes')?.value || '';
      pets.push({name, species, breed, dob, notes});
    });
    // client-side basic validation
    if(!customer.name || !customer.phone){ alert('Vui lòng nhập tên và số điện thoại.'); return; }

    try {
      const res = await fetch('/api/customers', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({ fullName: customer.name, phone: customer.phone, email: customer.email, address: customer.address, pets }) });
      if(!res.ok){ const txt = await res.text(); alert('Lỗi khi lưu: '+txt); return; }
      const txt = await res.text();
      alert('Lưu thành công: '+txt);
      // optionally reset form
    } catch(err){ console.error(err); alert('Lỗi khi gửi dữ liệu'); }
  });

  // Cancel all -> require PIN
  const cancelAll = document.getElementById('cancel-all');
  if(cancelAll) cancelAll.addEventListener('click', ()=>{
    openPinModal(()=>{
      document.getElementById('customer-name').value=''; document.getElementById('phone').value=''; document.getElementById('email').value=''; document.getElementById('address').value=''; petList.innerHTML=''; addPetBtn.click();
    });
  });

})();
