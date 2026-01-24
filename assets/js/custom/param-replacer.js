(function() {
  'use strict';

  function escapeRegex(str) {
    return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  }

  // DOM이 완전히 로드된 후 실행
  function initParamReplacer() {
    const inputs = document.querySelectorAll('.param-replacer-input');

    if (!inputs.length) {
      console.log('ParamReplacer: Input elements not found');
      return;
    }

    // 원본 코드 저장 (한 번만)
    const codeBlocks = document.querySelectorAll('.page__content pre code, .page__content code');
    const originalContents = new Map();

    codeBlocks.forEach((block, index) => {
      const content = block.textContent || block.innerText;
      originalContents.set(index, content);
    });

    function getCurrentReplacements() {
      const replaceMap = [];

      inputs.forEach((input) => {
        const key = input.dataset.key;
        const defaultValue = input.dataset.default;

        const value = input.value && input.value.trim() ? input.value : defaultValue;
        replaceMap.push({ key, search: defaultValue, replace: value });
      });

      return replaceMap;
    }

    // 교체 적용
    function applyReplacements() {
      const replaceMap = getCurrentReplacements();

      codeBlocks.forEach((block, index) => {
        const original = originalContents.get(index);
        if (!original) return;

        let updated = original;
        replaceMap.forEach(({ search, replace }) => {
          if (!search) return;
          const regex = new RegExp(escapeRegex(search), 'g');
          updated = updated.replace(regex, replace);
        });

        if (block.textContent !== updated) {
          block.textContent = updated;
        }
      });
    }

    // 이벤트 등록
    inputs.forEach((input) => {
      input.addEventListener('input', applyReplacements);
    });

    // 초기 적용
    applyReplacements();
  }

  // DOM 준비 완료 후 실행
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initParamReplacer);
  } else {
    initParamReplacer();
  }

})();
