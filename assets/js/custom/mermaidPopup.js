document.addEventListener('DOMContentLoaded', function() {
    // 모든 mermaid 다이어그램에 클릭 이벤트 추가
    document.querySelectorAll('.mermaid').forEach(function(mermaid) {
        mermaid.style.cursor = 'pointer';
        mermaid.addEventListener('click', function() {
            // 팝업 창 생성
            const popup = document.createElement('div');
            popup.style.position = 'fixed';
            popup.style.top = '0';
            popup.style.left = '0';
            popup.style.width = '100%';
            popup.style.height = '100%';
            popup.style.backgroundColor = 'rgba(0, 0, 0, 0.8)';
            popup.style.display = 'flex';
            popup.style.justifyContent = 'center';
            popup.style.alignItems = 'center';
            popup.style.zIndex = '9999';
            
            // SVG 복제
            const svg = mermaid.querySelector('svg').cloneNode(true);
            
            // SVG 컨테이너 생성
            const svgContainer = document.createElement('div');
            svgContainer.style.position = 'relative';
            svgContainer.style.width = '95%';
            svgContainer.style.height = '95%';
            svgContainer.style.display = 'flex';
            svgContainer.style.justifyContent = 'center';
            svgContainer.style.alignItems = 'center';
            svgContainer.style.backgroundColor = '#0D1117';
            svgContainer.style.borderRadius = '8px';
            svgContainer.style.padding = '20px';
            svgContainer.style.overflow = 'auto';
            
            // SVG 크기 조정
            svg.style.width = '100%';
            svg.style.height = '100%';
            svg.style.minWidth = '300px'; // 최소 너비 설정
            svg.style.minHeight = '200px'; // 최소 높이 설정
            
            // 닫기 버튼 추가
            const closeButton = document.createElement('button');
            closeButton.innerHTML = '×';
            closeButton.style.position = 'absolute';
            closeButton.style.top = '10px';
            closeButton.style.right = '10px';
            closeButton.style.width = '30px';
            closeButton.style.height = '30px';
            closeButton.style.border = 'none';
            closeButton.style.borderRadius = '50%';
            closeButton.style.backgroundColor = '#0D1117';
            closeButton.style.color = 'white';
            closeButton.style.fontSize = '20px';
            closeButton.style.cursor = 'pointer';
            closeButton.style.display = 'flex';
            closeButton.style.justifyContent = 'center';
            closeButton.style.alignItems = 'center';
            
            svgContainer.appendChild(svg);
            svgContainer.appendChild(closeButton);
            popup.appendChild(svgContainer);
            document.body.appendChild(popup);
            
            // 스크롤 방지
            document.body.style.overflow = 'hidden';
            
            // 닫기 이벤트
            const closePopup = function() {
                document.body.removeChild(popup);
                document.body.style.overflow = 'auto';
            };
            
            closeButton.addEventListener('click', closePopup);
            popup.addEventListener('click', function(e) {
                if (e.target === popup) {
                    closePopup();
                }
            });
            
            // ESC 키로 닫기
            document.addEventListener('keydown', function(e) {
                if (e.key === 'Escape') {
                    closePopup();
                }
            });
        });
    });
});