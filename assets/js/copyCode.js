document.addEventListener('DOMContentLoaded', function () {
  const codeBlocks = document.querySelectorAll('div.highlighter-rouge');

  codeBlocks.forEach((codeBlock) => {
    const copyButton = document.createElement('div');
    copyButton.className = 'copy-code-button';
    copyButton.innerHTML = '<img src="/assets/icon/copy.svg" alt="Copy"/>';

    copyButton.addEventListener('click', () => {
      if (copyButton.disabled) return;
      copyButton.disabled = true;
      const code = codeBlock.querySelector('code').innerText.trim();
      navigator.clipboard.writeText(code).then(() => {
        copyButton.innerHTML = `<div style="display: flex;"><p style="margin: auto 0" >copied</p><img src="/assets/icon/copied.svg" alt="Copied!" /></div>`;
        copyButton.style.border = 'border: 2px solid #3E3E3E';
        setTimeout(() => {
          copyButton.innerHTML = '<img src="/assets/icon/copy.svg" alt="Copy" />';
          copyButton.disabled = false;
        }, 2000);
      });
    });

    codeBlock.prepend(copyButton);
  });
});

