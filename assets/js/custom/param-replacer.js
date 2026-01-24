// 파라미터 대체 기능 (param_replace 설정이 있는 포스트용)
(function() {
  'use strict';

  function initParamReplacer() {
    const inputs = document.querySelectorAll('.param-replacer-input');
    if (!inputs.length) {
      return;
    }

    inputs.forEach((input) => {
      // 원본 코드 블록 저장 (각 param-replacer-container마다)
      const container = input.closest('.param-replacer-container');
      if (!container) {
        return;
      }

      const key = input.dataset.key;
      const codeBlocks = container.parentElement.querySelectorAll('.highlighter-rouge');
      const originalTexts = new Map();

      codeBlocks.forEach((block) => {
        originalTexts.set(block, block.textContent);
      });

      // input 변경 시 처리
      input.addEventListener('change', function() {
        const value = this.value;
        const targets = this.dataset.targets ? this.dataset.targets.split(',').map(s => s.trim()) : [];

        // 코드 블록 업데이트
        codeBlocks.forEach((block) => {
          let text = originalTexts.get(block);
          
          targets.forEach((target) => {
            const regex = new RegExp(escapeRegex(target), 'g');
            text = text.replace(regex, value);
          });

          block.textContent = text;
        });

        // 영향받는 코드 블록 목록 업데이트
        updateAffectedList(container, key, targets, value);
      });

      input.addEventListener('input', function() {
        const value = this.value;
        const targets = this.dataset.targets ? this.dataset.targets.split(',').map(s => s.trim()) : [];
        updateAffectedList(container, key, targets, value);
      });
    });

    function escapeRegex(string) {
      return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    }

    function updateAffectedList(container, key, targets, value) {
      const list = container.querySelector('.param-affected-list');
      if (!list) {
        return;
      }

      const codeBlocks = container.parentElement.querySelectorAll('.page__content pre code');
      const affectedBlocks = [];
      const codeBlocksInParent = container.parentElement.querySelectorAll('.highlighter-rouge');

      codeBlocksInParent.forEach((block, index) => {
        let isAffected = false;
        const blockText = block.textContent;

        targets.forEach((target) => {
          if (blockText.includes(target)) {
            isAffected = true;
          }
        });

        if (isAffected) {
          affectedBlocks.push({
            element: block,
            caption: block.dataset.codeBoxCaption || 'Code Block ' + (index + 1),
            index: index
          });
        }
      });

      // 목록 업데이트
      list.innerHTML = '';
      const count = container.querySelector('.param-count');
      if (count) {
        count.textContent = affectedBlocks.length;
      }

      affectedBlocks.forEach((block) => {
        const li = document.createElement('button');
        li.className = 'param-affected-item';
        li.type = 'button';
        li.innerHTML = `<code>${block.caption}</code>`;
        li.setAttribute('tabindex', '0');

        li.addEventListener('click', function() {
          block.element.scrollIntoView({ behavior: 'smooth', block: 'start' });
        });

        li.addEventListener('keypress', function(e) {
          if (e.key === 'Enter') {
            block.element.scrollIntoView({ behavior: 'smooth', block: 'start' });
          }
        });

        li.addEventListener('mouseenter', function() {
          this.focus();
        });

        list.appendChild(li);
      });

      // 리스트 네비게이션 (화살표 키)
      list.addEventListener('keydown', function(e) {
        if (e.key === 'ArrowDown' || e.key === 'ArrowUp') {
          e.preventDefault();

          const buttons = Array.from(list.querySelectorAll('button'));
          if (!buttons.length) {
            return;
          }

          const currentIndex = buttons.indexOf(document.activeElement);
          let nextIndex;

          if (e.key === 'ArrowDown') {
            nextIndex = (currentIndex + 1) % buttons.length;
          } else {
            nextIndex = (currentIndex - 1 + buttons.length) % buttons.length;
          }

          buttons[nextIndex].focus();
        }
      });
    }
  }

  // DOM 준비 완료 후 실행
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initParamReplacer);
  } else {
    initParamReplacer();
  }

})();

// 파라미터 목차 모달 (Control+Shift+E로 열기)
(function() {
  'use strict';

  function initParamModal() {
    const paramContainers = document.querySelectorAll('.param-replacer-container');
    if (!paramContainers.length) {
      return;
    }

    // 모달 스타일
    const modalStyles = document.createElement('style');
    modalStyles.textContent = `
      #param-modal-overlay {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0, 0, 0, 0.7);
        z-index: 10000;
        display: none;
        align-items: center;
        justify-content: center;
        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
      }

      #param-modal-overlay.open {
        display: flex;
      }

      #param-modal {
        background: #1e1e1e;
        border-radius: 8px;
        width: 90%;
        max-width: 500px;
        max-height: 70vh;
        overflow: hidden;
        display: flex;
        flex-direction: column;
        box-shadow: 0 10px 40px rgba(0,0,0,0.5);
        color: #d5d5d6;
      }

      #param-modal-header {
        padding: 16px 20px;
        border-bottom: 1px solid rgba(255, 255, 255, 0.1);
      }

      #param-modal-title {
        margin: 0;
        font-size: 18px;
        font-weight: 600;
        color: #e3e6ed;
      }

      #param-modal-content {
        flex: 1;
        overflow-y: auto;
      }

      #param-modal-list {
        list-style: none;
        margin: 0;
        padding: 0;
      }

      .param-modal-item {
        padding: 12px 16px;
        border-bottom: 1px solid rgba(255, 255, 255, 0.05);
        cursor: pointer;
        transition: all 0.2s ease;
        color: #cbd5e1;
        font-size: 14px;
        display: flex;
        justify-content: space-between;
        align-items: center;
        background: none;
        border: none;
        width: 100%;
        text-align: left;
      }

      .param-modal-item:hover {
        background: rgba(91, 127, 199, 0.15);
        color: #d5d5d6;
        padding-left: 20px;
      }

      .param-modal-item:focus {
        outline: none;
        background: rgba(91, 127, 199, 0.25);
        color: #89ddff;
      }

      .param-modal-item.active {
        background: rgba(91, 127, 199, 0.25);
        color: #89ddff;
      }

      #param-modal-footer {
        padding: 12px 16px;
        border-top: 1px solid rgba(255, 255, 255, 0.1);
        font-size: 12px;
        color: #9aa8c7;
      }

      #param-modal-toggle {
        position: fixed;
        right: 32px;
        bottom: 96px; /* back-to-top(32px) 위로 64px 여유 */
        z-index: 9999;
        background: #010409;
        color: #fff;
        border: none;
        border-radius: 4px;
        width: 40px;
        height: 40px;
        font-size: 18px;
        cursor: pointer;
        box-shadow: 0 2px 8px rgba(0,0,0,0.15);
        display: none;
        align-items: center;
        justify-content: center;
        padding: 0;
        transition: all 0.3s ease;
      }

      #param-modal-toggle:hover {
        background: #1a1d24;
      }

      #param-modal-toggle.show {
        display: flex;
      }

      #param-modal-toggle.active {
        background: #5b7fc7;
      }

      @media (max-width: 768px) {
        #param-modal {
          width: 95%;
          max-width: 100%;
        }
      }
    `;
    document.head.appendChild(modalStyles);

    // 모달 구조 생성
    const overlay = document.createElement('div');
    overlay.id = 'param-modal-overlay';

    const modal = document.createElement('div');
    modal.id = 'param-modal';

    const header = document.createElement('div');
    header.id = 'param-modal-header';

    const title = document.createElement('h2');
    title.id = 'param-modal-title';
    title.textContent = '파라미터 설정';
    header.appendChild(title);
    modal.appendChild(header);

    const content = document.createElement('div');
    content.id = 'param-modal-content';

    const list = document.createElement('ul');
    list.id = 'param-modal-list';

    // 파라미터 항목 추가
    paramContainers.forEach((container, index) => {
      const label = container.querySelector('label[for]');
      const labelText = label ? label.textContent.trim() : 'Parameter ' + index;
      
      const li = document.createElement('button');
      li.className = 'param-modal-item';
      li.type = 'button';
      li.dataset.paramIndex = index;
      
      const text = document.createElement('span');
      text.textContent = labelText;
      li.appendChild(text);

      li.addEventListener('click', function() {
        container.scrollIntoView({ behavior: 'smooth', block: 'start' });
        const input = container.querySelector('.param-replacer-input');
        if (input) {
          setTimeout(() => input.focus(), 300);
        }
        closeModal();
      });

      list.appendChild(li);
    });

    content.appendChild(list);
    modal.appendChild(content);

    const footer = document.createElement('div');
    footer.id = 'param-modal-footer';
    footer.textContent = 'Ctrl+Shift+E로 열기/닫기 | ESC로 닫기';
    modal.appendChild(footer);

    overlay.appendChild(modal);
    document.body.appendChild(overlay);

    // 토글 버튼
    const toggleBtn = document.createElement('button');
    toggleBtn.id = 'param-modal-toggle';
    toggleBtn.type = 'button';
    toggleBtn.title = '파라미터 설정 (Ctrl+Shift+E)';
    toggleBtn.innerHTML = '<i class="fa-solid fa-list"></i>';
    document.body.appendChild(toggleBtn);

    // 모달 제어 함수
    function openModal() {
      overlay.classList.add('open');
      toggleBtn.classList.add('active');
      // 첫 번째 항목에 포커스
      const firstItem = list.querySelector('button');
      if (firstItem) {
        firstItem.focus();
      }
    }

    function closeModal() {
      overlay.classList.remove('open');
      toggleBtn.classList.remove('active');
    }

    function toggleModal() {
      overlay.classList.toggle('open');
      toggleBtn.classList.toggle('active');
      if (overlay.classList.contains('open')) {
        const firstItem = list.querySelector('button');
        if (firstItem) {
          firstItem.focus();
        }
      }
    }

    // 이벤트 리스너
    toggleBtn.addEventListener('click', toggleModal);

    // Control+Shift+E로 토글
    document.addEventListener('keydown', function(e) {
      if (e.ctrlKey && e.shiftKey && e.key === 'E') {
        e.preventDefault();
        toggleModal();
      }
    });

    // ESC로 닫기
    document.addEventListener('keydown', function(e) {
      if (e.key === 'Escape' && overlay.classList.contains('open')) {
        closeModal();
      }
    });

    // 모달 내에서 화살표 키 네비게이션
    list.addEventListener('keydown', function(e) {
      if (e.key === 'ArrowDown' || e.key === 'ArrowUp') {
        e.preventDefault();
        const buttons = Array.from(list.querySelectorAll('button'));
        const currentIndex = buttons.indexOf(document.activeElement);
        let nextIndex;

        if (e.key === 'ArrowDown') {
          nextIndex = (currentIndex + 1) % buttons.length;
        } else {
          nextIndex = (currentIndex - 1 + buttons.length) % buttons.length;
        }

        buttons[nextIndex].focus();
      }
    });

    // 오버레이 클릭으로 닫기
    overlay.addEventListener('click', function(e) {
      if (e.target === this) {
        closeModal();
      }
    });

    // 스크롤에 따라 버튼 표시/숨김
    window.addEventListener('scroll', function() {
      const isNearBottom = window.innerHeight + window.scrollY >= document.documentElement.scrollHeight - 100;
      if (window.scrollY > 200 && !isNearBottom) {
        toggleBtn.classList.add('show');
      } else {
        toggleBtn.classList.remove('show');
        closeModal();
      }
    });
  }

  // DOM 준비 완료 후 실행
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initParamModal);
  } else {
    initParamModal();
  }

})();
