document.addEventListener('DOMContentLoaded', function () {
  const codeBlocks = document.querySelectorAll('div.highlighter-rouge, figure.highlight');

  // 스타일 추가
  const style = document.createElement('style');
  style.textContent = `
    .copy-code-button {
      z-index: 10;
      position: absolute;
      top: 40px;
      right: 8px;
      padding: 6px 12px;
      background: #2d2d2d;
      border: 1px solid #2d2d2d;
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

    .copy-code-button img {
      margin-bottom: 0;
    }

    .copy-code-button:hover {
      background: #2d2d2d;
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
      background: #2d2d2d;
      border-color: #2d2d2d;
    }

    .highlighter-rouge {
      position: relative;
    }

    .highlighter-rouge:hover .copy-code-button {
      opacity: 1;
    }

    .highlight:hover .copy-code-button {
      opacity: 1;
    }

    .copy-code-button p {
      margin: 0;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
    }

    .global-copy-notice {
      position: fixed;
      bottom: 20px;
      left: 50%;
      transform: translateX(-50%);
      background: rgba(0, 0, 0, 0.8);
      color: #fff;
      padding: 10px 20px;
      border-radius: 4px;
      font-size: 14px;
      display: flex;
      align-items: center;
      gap: 8px;
      opacity: 0;
      transition: opacity 0.3s ease;
      pointer-events: none;
      z-index: 1000;
    }

    .global-copy-notice.show {
      opacity: 1;
    }

    .highlighter-rouge,
    .highlight {
      overflow-x: auto;
    }

    .highlighter-rouge pre,
    .highlight pre,
    .highlighter-rouge code,
    .highlight code {
      white-space: pre;
      overflow-x: auto;
    }

    .code-language-header {
      background: #1e1e1e;
      border-bottom: 1px solid #3d3d3d;
      padding: 6px 12px;
      font-size: 11px;
      font-weight: 400;
      color: #7a7a7a;
      letter-spacing: 0.5px;
      text-align: right;
      border-radius: 6px 6px 0 0;
    }

    .highlighter-rouge .highlight,
    figure.highlight .highlight {
      border-radius: 0 0 6px 6px;
    }
  `;
  document.head.appendChild(style);

  // 언어 정보 추출 함수
  function getLanguage(codeBlock) {
    const classes = codeBlock.className;
    const match = classes.match(/language-([\w]+)/);
    if (match && match[1]) {
      const lang = match[1].toLowerCase();
      // 언어명 표준화
      const languageMap = {
        'java': 'Java',
        'sql': 'SQL',
        'javascript': 'JavaScript',
        'js': 'JavaScript',
        'html': 'HTML',
        'css': 'CSS',
        'xml': 'XML',
        'json': 'JSON',
        'python': 'Python',
        'bash': 'Bash',
        'sh': 'Bash',
        'ruby': 'Ruby',
        'php': 'PHP',
        'swift': 'Swift',
        'kotlin': 'Kotlin',
        'go': 'Go',
        'rust': 'Rust',
        'typescript': 'TypeScript',
        'ts': 'TypeScript',
        'vue': 'Vue',
        'react': 'React',
        'scss': 'SCSS',
        'sass': 'SASS',
        'less': 'LESS',
        'yaml': 'YAML',
        'yml': 'YAML',
        'markdown': 'Markdown',
        'md': 'Markdown'
      };
      return languageMap[lang] || lang.charAt(0).toUpperCase() + lang.slice(1);
    }
    return null;
  }

  codeBlocks.forEach((codeBlock) => {
    let codeElem = codeBlock.querySelector('table.rouge-table .code pre') || codeBlock.querySelector('code');
    if (!codeElem) return;

    // 언어 헤더 추가
    const language = getLanguage(codeBlock);
    if (language) {
      const languageHeader = document.createElement('div');
      languageHeader.className = 'code-language-header';
      languageHeader.textContent = language;
      codeBlock.insertBefore(languageHeader, codeBlock.firstChild);
    }

    // 브라우저 하단 복사 알림 요소 생성 (한 번만)
    let globalNotice = document.querySelector('.global-copy-notice');
    if (!globalNotice) {
      globalNotice = document.createElement('div');
      globalNotice.className = 'global-copy-notice';
      globalNotice.textContent = '복사되었습니다!';
      document.body.appendChild(globalNotice);
    }

    const copyButton = document.createElement('div');
    copyButton.className = 'copy-code-button';
    copyButton.innerHTML = `
      <img src="/assets/icon/copy.svg" alt="전체 복사"/>
      <p>전체 복사</p>
    `;

    let timer = null;
    function copyCodeAction() {
      if (copyButton.disabled) return;
      copyButton.disabled = true;
      const code = codeElem.innerText.trim();
      navigator.clipboard.writeText(code).then(() => {
        copyButton.classList.add('copied');
        copyButton.innerHTML = `
          <img src="/assets/icon/copied.svg" alt="복사 완료!" />
          <p>복사 완료!</p>
        `;
        clearTimeout(timer);
        timer = setTimeout(() => {
          copyButton.classList.remove('copied');
          copyButton.innerHTML = `
            <img src="/assets/icon/copy.svg" alt="전체 복사" />
            <p>전체 복사</p>
          `;
          copyButton.disabled = false;
        }, 2000);
        // 브라우저 하단 복사 알림 표시
        globalNotice.textContent = '복사되었습니다!';
        globalNotice.style.opacity = '1';
        setTimeout(() => {
          globalNotice.style.opacity = '0';
        }, 1200);
      });
    }

    copyButton.addEventListener('click', copyCodeAction);
    // <code> 태그 클릭 시 전체 복사
    if (codeElem.tagName.toLowerCase() === 'code') {
      codeElem.style.cursor = 'pointer';
      codeElem.addEventListener('click', copyCodeAction);
    }
    codeBlock.append(copyButton);
  });

  // 일반 텍스트 내 인라인 코드 복사 기능 추가
  // <code class="language-plaintext highlighter-rouge"> 클릭 시 복사
  document.querySelectorAll('code.language-plaintext.highlighter-rouge').forEach(function(codeElem) {
    codeElem.style.cursor = 'pointer';
    codeElem.title = '클릭하면 복사됩니다';
    codeElem.addEventListener('click', function() {
      const code = codeElem.innerText.trim();
      navigator.clipboard.writeText(code).then(() => {
        // 브라우저 하단 복사 알림 요소 생성 (한 번만)
        let globalNotice = document.querySelector('.global-copy-notice');
        if (!globalNotice) {
          globalNotice = document.createElement('div');
          globalNotice.className = 'global-copy-notice';
          globalNotice.textContent = '복사되었습니다!';
          document.body.appendChild(globalNotice);
        }
        globalNotice.textContent = '복사되었습니다!';
        globalNotice.style.opacity = '1';
        setTimeout(() => {
          globalNotice.style.opacity = '0';
        }, 1200);
      });
    });
  });
});