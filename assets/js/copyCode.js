document.addEventListener('DOMContentLoaded', function () {
  const codeBlocks = document.querySelectorAll('div.highlighter-rouge');

  // 스타일 추가
  const style = document.createElement('style');
  style.textContent = `
    .copy-code-button {
      position: absolute;
      top: 8px;
      right: 8px;
      padding: 6px 12px;
      background: #2d2d2d;
      border: 1px solid #3d3d3d;
      border-radius: 6px;
      color: #fff;
      font-size: 13px;
      font-weight: 500;
      cursor: pointer;
      display: flex;
      align-items: center;
      gap: 6px;
      transition: all 0.2s ease;
      opacity: 0;
    }

    .copy-code-button:hover {
      background: #3d3d3d;
      transform: translateY(-1px);
    }

    .copy-code-button:active {
      transform: translateY(0);
    }

    .copy-code-button img {
      width: 16px;
      height: 16px;
      filter: invert(1);
    }

    .copy-code-button.copied {
      background: #28a745;
      border-color: #28a745;
    }

    .highlighter-rouge {
      position: relative;
    }

    .highlighter-rouge:hover .copy-code-button {
      opacity: 1;
    }

    .copy-code-button p {
      margin: 0;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
    }
  `;
  document.head.appendChild(style);

  codeBlocks.forEach((codeBlock) => {
    const copyButton = document.createElement('div');
    copyButton.className = 'copy-code-button';
    copyButton.innerHTML = `
      <img src="/assets/icon/copy.svg" alt="전체 복사"/>
      <p>전체 복사</p>
    `;

    copyButton.addEventListener('click', () => {
      if (copyButton.disabled) return;
      copyButton.disabled = true;
      
      const code = codeBlock.querySelector('code').innerText.trim();
      navigator.clipboard.writeText(code).then(() => {
        copyButton.classList.add('copied');
        copyButton.innerHTML = `
          <img src="/assets/icon/copied.svg" alt="Copied!" />
          <p>Copied!</p>
        `;
        
        setTimeout(() => {
          copyButton.classList.remove('copied');
          copyButton.innerHTML = `
            <img src="/assets/icon/copy.svg" alt="전체 복사" />
            <p>전체 복사</p>
          `;
          copyButton.disabled = false;
        }, 2000);
      });
    });

    codeBlock.prepend(copyButton);
  });
});