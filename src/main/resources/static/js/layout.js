// ===========================섹션제어========================
let sectionStack = [];
// {type : "static", id : 1}
// or
// {type : "dynamic", header: html, content : html}
let currentSection = 1;
let isAnimating = false;
let isSubmitting = false;

const sectionMap = {
    1: 'homeSection',
    2: 'menuSection',
    3: 'searchSection',
    4: 'wishSection',
    5: 'mypageSection',
    6: 'dynamicSection'
};

const sectionNavMaxLength = 3;

// 스크롤 방향 추적을 위한 변수
let lastScrollTop = 0;
let scrollThreshold = 10; // 스크롤 최소 거리 (너무 작은 움직임은 무시)

document.addEventListener('DOMContentLoaded', () => {
    loadRecentSearches();
    initSearchForm();
    updateAutoSaveText();
    // 초기 푸터 네비게이션 활성화 상태 설정 (홈)
    updateFooterNav(1);
    // 스크롤 이벤트 리스너 초기화
    initScrollHideNav();
    
    // 로그인 후 예약 정보 확인
    checkReservationAfterLogin();
});

// 로그인 후 예약 정보 확인 및 모달 열기
async function checkReservationAfterLogin() {
    try {
        // 결제 페이지로 리다이렉트 확인 (다음 단계 버튼 클릭 후 로그인)
        const paymentRedirectResponse = await fetch('/api/check-payment-redirect');
        if (paymentRedirectResponse.ok) {
            const paymentRedirectData = await paymentRedirectResponse.json();
            if (paymentRedirectData && paymentRedirectData.url) {
                // 결제 페이지로 이동
                setTimeout(() => {
                    if (typeof addModal === 'function') {
                        addModal(paymentRedirectData.url);
                    } else if (typeof fetchSection === 'function') {
                        fetchSection(paymentRedirectData.url);
                    }
                }, 500);
                return; // 결제 페이지로 리다이렉트가 있으면 다른 정보는 확인하지 않음
            }
        }
        
        // 항공편 예약 정보 확인
        const airResponse = await fetch('/api/check-air-reservation');
        if (airResponse.ok) {
            const airReservationData = await airResponse.json();
            if (airReservationData && Object.keys(airReservationData).length > 0) {
                // 항공편 예약 정보가 있으면 항공편 상세 페이지로 이동 후 결제 페이지로 이동
                setTimeout(() => {
                    // 먼저 항공편 상세 페이지로 요청하여 detailResult를 세션에 저장
                    fetch('/air/detail', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(airReservationData)
                    })
                    .then(() => {
                        // 결제 페이지로 이동
                        if (typeof fetchSection === 'function') {
                            fetchSection('/air/purchase/payment');
                        } else if (typeof addModal === 'function') {
                            addModal('/air/purchase/payment');
                        }
                    })
                    .catch(err => console.error('항공편 상세 정보 로드 실패:', err));
                }, 500);
                return; // 항공편 예약 정보가 있으면 일반 예약 정보는 확인하지 않음
            }
        }
        
        // 일반 상품 예약 정보 확인
        const response = await fetch('/product/reservation/check');
        if (response.ok) {
            const reservationData = await response.json();
            if (reservationData && Object.keys(reservationData).length > 0) {
                // 예약 정보가 있으면 예약 모달 열기
                setTimeout(() => {
                    if (typeof addModal === 'function') {
                        addModal('product/reservation', false, reservationData);
                    }
                }, 500); // 페이지 로드 후 약간의 지연
            }
        }
    } catch (error) {
        console.error('예약 정보 확인 실패:', error);
    }
}

function showSection(sectionIndex, push = true) {
    if (currentSection === sectionIndex) return;
    if (isAnimating) return;

    // 모달이 열려있으면 먼저 닫기
    if (fullModal && !fullModal.classList.contains('hide')) {
        closeModal();
    }

    isAnimating = true;

    // 섹션 이동 기록
    if (push) {
        sectionStack.push({ type: "static", id: currentSection });
        if (sectionStack.length > sectionNavMaxLength) sectionStack.shift();
    }

    const startSection = document.getElementById(sectionMap[currentSection]);
    const targetSection = document.getElementById(sectionMap[sectionIndex]);

    // 출발 section hidden
    startSection.classList.remove('active');
    startSection.classList.add('exit');

    // 대상 section active
    targetSection.classList.remove('exit');
    targetSection.classList.add('active');

    // 푸터 네비게이션 활성화 상태 업데이트
    updateFooterNav(sectionIndex);

    // 섹션 변경 시 네비게이션 바 보이기 및 스크롤 위치 초기화
    const footer = document.getElementById('footerSection');
    if (footer) {
        footer.classList.remove('hidden');
    }
    lastScrollTop = 0;
    
    // 섹션 변경 후 스크롤 리스너 재설정
    setTimeout(() => {
        addScrollListeners();
    }, 100);

    // 애니메이팅 완료 처리
    setTimeout(() => {
        startSection.classList.remove('exit');
        isAnimating = false;
    }, 200);

    currentSection = sectionIndex;
}

// 푸터 네비게이션 활성화 상태 업데이트
function updateFooterNav(sectionIndex) {
    const navItems = document.querySelectorAll('#footerSection .nav-item');
    navItems.forEach(item => {
        const itemSection = parseInt(item.getAttribute('data-section'));
        if (itemSection === sectionIndex) {
            item.classList.add('active');
        } else {
            item.classList.remove('active');
        }
    });
}

// 스크롤 가능한 모든 요소에 리스너 추가
function addScrollListeners() {
    // 메인 섹션 스크롤 이벤트
    const mainSection = document.getElementById('mainSection');
    if (mainSection && !mainSection.hasAttribute('data-scroll-listener')) {
        mainSection.addEventListener('scroll', handleScroll, { passive: true });
        mainSection.setAttribute('data-scroll-listener', 'true');
    }

    // 모든 섹션의 section-body 스크롤 이벤트
    const sectionBodies = document.querySelectorAll('.section-body');
    sectionBodies.forEach(body => {
        if (!body.hasAttribute('data-scroll-listener')) {
            body.addEventListener('scroll', handleScroll, { passive: true });
            body.setAttribute('data-scroll-listener', 'true');
        }
    });

    // 모달 내부의 스크롤 가능한 요소들도 감지
    const modalBody = document.getElementById('modalBody');
    if (modalBody) {
        // modal-slide 자체도 스크롤 가능할 수 있음
        const modalSlides = modalBody.querySelectorAll('.modal-slide');
        modalSlides.forEach(slide => {
            const style = window.getComputedStyle(slide);
            if ((style.overflowY === 'auto' || style.overflowY === 'scroll' || style.overflow === 'auto' || style.overflow === 'scroll') && 
                !slide.hasAttribute('data-scroll-listener')) {
                slide.addEventListener('scroll', handleScroll, { passive: true });
                slide.setAttribute('data-scroll-listener', 'true');
            }
        });
        
        // 모달 내부의 다른 스크롤 가능한 요소들
        const modalScrollable = modalBody.querySelectorAll('.payment-container, .payment-body, [style*="overflow-y"], [style*="overflow"]');
        modalScrollable.forEach(el => {
            const style = window.getComputedStyle(el);
            if ((style.overflowY === 'auto' || style.overflowY === 'scroll' || 
                 el.classList.contains('payment-container') || el.classList.contains('payment-body')) && 
                !el.hasAttribute('data-scroll-listener')) {
                el.addEventListener('scroll', handleScroll, { passive: true });
                el.setAttribute('data-scroll-listener', 'true');
            }
        });
    }

    // 스크롤 가능한 다른 컨테이너들도 감지 (overflow-y: auto 또는 scroll인 요소)
    const scrollableElements = document.querySelectorAll('[style*="overflow-y"], [style*="overflow"]');
    scrollableElements.forEach(el => {
        const style = window.getComputedStyle(el);
        if ((style.overflowY === 'auto' || style.overflowY === 'scroll') && 
            !el.hasAttribute('data-scroll-listener')) {
            el.addEventListener('scroll', handleScroll, { passive: true });
            el.setAttribute('data-scroll-listener', 'true');
        }
    });
}

// 스크롤에 따라 네비게이션 바 숨기기/보이기 초기화
function initScrollHideNav() {
    // 초기 리스너 추가
    addScrollListeners();

    // 동적 섹션 및 전체 문서 변경 감지
    const observer = new MutationObserver(() => {
        // 새로운 스크롤 가능한 요소가 추가될 때마다 리스너 추가
        addScrollListeners();
    });

    // 전체 문서를 관찰 (모든 섹션 변경 감지)
    observer.observe(document.body, { 
        childList: true, 
        subtree: true,
        attributes: true,
        attributeFilter: ['style', 'class']
    });
}

// 스크롤 이벤트 핸들러
function handleScroll(event) {
    const footer = document.getElementById('footerSection');
    if (!footer) return;

    const scrollContainer = event.target;
    const currentScrollTop = scrollContainer.scrollTop;

    // 맨 위에 있으면 항상 네비게이션 바 보이기
    if (currentScrollTop <= scrollThreshold) {
        footer.classList.remove('hidden');
        lastScrollTop = 0;
        return;
    }

    // 스크롤 방향 감지
    if (Math.abs(currentScrollTop - lastScrollTop) < scrollThreshold) {
        return; // 최소 스크롤 거리 미만이면 무시
    }

    if (currentScrollTop > lastScrollTop) {
        // 아래로 스크롤 (위로 올라감) → 네비게이션 바 숨기기
        footer.classList.add('hidden');
    } else if (currentScrollTop < lastScrollTop) {
        // 위로 스크롤 (아래로 내려감) → 네비게이션 바 보이기
        footer.classList.remove('hidden');
    }

    lastScrollTop = currentScrollTop;
}

function fetchSection(endPoint, payload = null) {
    fetchContent(endPoint, payload).then(html => {
        const container = document.getElementById('dynamicSection');
        if (!container) {
            console.error('dynamicSection not found');
            return;
        }
        
        const sectionBody = container.querySelector('.section-body > div');
        
        // section-body > div가 없으면 fallback: 기존 방식 사용
        if (!sectionBody) {
            console.warn('section-body > div not found, using fallback');
            // 기존 내용 스택에 저장
            if (currentSection === 6) {
                sectionStack.push({
                    type: 'dynamic',
                    content: container.innerHTML
                });
                if (sectionStack.length > sectionNavMaxLength) sectionStack.shift();
            }
            // 전체 교체 (fallback)
            container.innerHTML = html;
            executeScripts(container);
        } else {
            // 기존 내용 스택에 저장
            if (currentSection === 6) {
                sectionStack.push({
                    type: 'dynamic',
                    content: sectionBody.innerHTML
                });
                if (sectionStack.length > sectionNavMaxLength) sectionStack.shift();
            }

            // section-body 안의 div에만 내용 교체 (section-header, section-body 구조 유지)
            sectionBody.innerHTML = html;

            // 스크립트 실행
            executeScripts(sectionBody);
        }

        // 현재 섹션 표시
        if (currentSection !== 6) showSection(6);
        
        // 동적 섹션 로드 후 스크롤 리스너 재설정
        setTimeout(() => {
            addScrollListeners();
        }, 100);
    });
}

function prevSection() {
    if (isAnimating) return;

    const prev = sectionStack.pop();

    if (!prev) {
        showSection(1, false);
        return;
    }

    if (prev.type === 'static') {
        // push 방지 옵션 사용
        showSection(prev.id, false);
    } else if (prev.type === 'dynamic') {
        const container = document.getElementById('dynamicSection');
        if (!container) {
            console.error('dynamicSection not found');
            return;
        }
        
        const sectionBody = container.querySelector('.section-body > div');
        if (sectionBody) {
            // section-body > div에만 복원
            sectionBody.innerHTML = prev.content;
        } else {
            // fallback: 전체 복원
            container.innerHTML = prev.content;
        }

        // 모든 section 상태 초기화 후 dynamicSection만 active
        container.classList.add('active');
        currentSection = 6;
        
        // 섹션 복원 후 스크롤 리스너 재설정
        setTimeout(() => {
            addScrollListeners();
        }, 100);
    }
}

// 로그인시 찜, 마이페이지 다시 fetch
async function refreshSectionsAfterLogin() {
    console.log("refreshSectionsAfterLogin1");

    const sectionConfigs = [
        { id: "wishSection", url: "/wish" },
        { id: "mypageSection", url: "/mypage" }
    ];

    try {
        // 병렬 요청
        const responses = await Promise.all(
            sectionConfigs.map(cfg =>
                fetch(cfg.url, { headers: { "X-Requested-With": "XMLHttpRequest" } })
            )
        );

        // 각각 텍스트로 변환
        const htmlFragments = await Promise.all(responses.map(res => res.text()));

        // 섹션별 교체
        sectionConfigs.forEach((cfg, index) => {
            const target = document.querySelector(`#${cfg.id} .section-body > div`);
            if (target) {
                target.innerHTML = htmlFragments[index];
            }
        });
        
        // 섹션 새로고침 후 스크롤 리스너 재설정
        setTimeout(() => {
            addScrollListeners();
        }, 100);

        console.log("refreshSectionsAfterLogin2");

    } catch (err) {
        console.error("갱신 실패:", err);
    }
}

// ===========================섹션 - 메뉴 탭=================
function showMenuTab(tabId) {
    const tabs = ['menu-travel', 'menu-support'];
    const tabButtons = ['tab-travel', 'tab-support'];

    // 탭 내용 전환
    tabs.forEach(id => {
        const menuElement = document.getElementById(id);
        if (!menuElement) return;

        if (id === tabId) {
            menuElement.classList.add('active');
        } else {
            menuElement.classList.remove('active');
        }
    });

    // 탭 버튼 활성화 상태 전환
    tabButtons.forEach(btnId => {
        const btn = document.getElementById(btnId);
        if (!btn) return;
        
        if ((btnId === 'tab-travel' && tabId === 'menu-travel') ||
            (btnId === 'tab-support' && tabId === 'menu-support')) {
            btn.classList.add('active');
        } else {
            btn.classList.remove('active');
        }
    });
}

// 메뉴 카테고리 확장/축소
function toggleMenuCategory(btn) {
    const category = btn.closest('.menu-category');
    const subcategory = category.querySelector('.menu-subcategory');
    const icon = btn.querySelector('.category-icon');
    
    if (subcategory) {
        subcategory.classList.toggle('active');
        if (subcategory.classList.contains('active')) {
            icon.classList.remove('fa-chevron-down');
            icon.classList.add('fa-chevron-up');
        } else {
            icon.classList.remove('fa-chevron-up');
            icon.classList.add('fa-chevron-down');
        }
    }
}

// ===========================모달제어========================
const fullModal = document.getElementById('fullModal');
const modalBody = document.getElementById('modalBody');

// 모달 스택
let modalStack = [];
let currentModal = null;

// 모달 열기 (첫 모달)
function openModal(endPoint, payload = null) {
    modalStack = []; // 스택 초기화
    fullModal.classList.remove('hide');
    fullModal.classList.add('show');
    // 모달이 열릴 때 배경 스크롤 막기
    if (document && document.body) {
        document.body.style.overflow = 'hidden';
    }

    // 새 모달 DOM 생성
    const newModal = document.createElement('div');
    newModal.classList.add('modal-slide', 'show');
    fetchContent(endPoint, payload).then(html => {
        newModal.innerHTML = html
        executeScripts(newModal);
        // 모달 내용 로드 후 스크롤 리스너 추가
        setTimeout(() => {
            addScrollListeners();
        }, 100);
    }); // 모달 내용물 fetch
    modalBody.appendChild(newModal);
    currentModal = newModal;
}

// 전역 스코프에 노출
window.openModal = openModal;

// 문자열 모달 열기
function openModalHtml(htmlString) {
    modalStack = [];
    fullModal.classList.remove('hide');
    fullModal.classList.add('show');
    // 모달이 열릴 때 배경 스크롤 막기
    if (document && document.body) {
        document.body.style.overflow = 'hidden';
    }

    const newModal = document.createElement('div');
    newModal.classList.add('modal-slide', 'show');

    // 전달받은 HTML 직접 주입
    newModal.innerHTML = htmlString;
    executeScripts(newModal);
    modalBody.appendChild(newModal);
    currentModal = newModal;
}

// 새 모달 내용 생성
function addModal(endPoint, flush = false, payload = null) {
    // 모달이 열려있지 않으면 먼저 열기
    if (!currentModal || fullModal.classList.contains('hide')) {
        openModal(endPoint, payload);
        return;
    }
    
    const newModal = document.createElement('div');
    newModal.classList.add('modal-slide', 'leave');

    modalBody.appendChild(newModal);
    modalStack.push(currentModal); // 기존 모달 스택에 저장
    currentModal = newModal; // 새 모달로 갱신

    // fetchContent 실행
    fetchContent(endPoint, payload)
        .then(html => {
            if (!html) throw new Error('Empty content'); // html 비어있으면 실패 처리

            // 모달 내용 주입 + 스크립트 실행
            newModal.innerHTML = html;
            executeScripts(newModal);

            // 슬라이드 인 애니메이션
            setTimeout(() => {
                newModal.classList.remove('leave');
                newModal.classList.add('show');
                // 모달 내용 로드 후 스크롤 리스너 추가
                setTimeout(() => {
                    addScrollListeners();
                }, 100);
            }, 0);

            // fetch 성공시 flush
            if (flush) {
                setTimeout(() => {
                    modalStack.forEach(modal => {
                        if (modal?.parentNode) {
                            modal.parentNode.removeChild(modal);
                        }
                    });
                    modalStack = [];
                }, 200);
            }
        })
        .catch(err => {
            console.error(`Fetch failed: ${err}`);
            alert(`Fetch error: ${err}`);

            // fetch 실패시 flush 무시, 이전 모달 복귀
            if (newModal.parentNode) {
                newModal.parentNode.removeChild(newModal);
            }
            backModal();
        });
}

// 전역 스코프에 노출
window.addModal = addModal;


// 뒤로가기
function backModal() {
    if (modalStack.length > 0) {
        const prevModal = modalStack.pop();

        // 현재 모달 나가기
        currentModal.classList.remove('show');
        currentModal.classList.add('leave');

        currentModal = prevModal;

        // 나간 모달 DOM 제거
        setTimeout(() => {
            modalBody.querySelectorAll('.leave').forEach(el => el.remove());
        }, 200);

    } else {
        closeModal();
    }
}

// 모달 닫기
function closeModal() {
    fullModal.classList.remove('show');
    fullModal.classList.add('hide');
    modalStack = [];
    currentModal = null;
    // 모달이 모두 닫혔을 때 배경 스크롤 다시 허용
    if (document && document.body) {
        document.body.style.overflow = '';
    }
    // 하단 네비게이션 바 다시 보이기 (모달이 모두 닫혔을 때)
    const footer = document.getElementById('footerSection');
    if (footer) footer.classList.remove('hidden');
    setTimeout(() => {
        modalBody.innerHTML = '';
    }, 500);
}

// 하프모달 열기
function openHalfModal(containerId) {
    console.log('containerId ', containerId);

    const content = document.getElementById(containerId);
    const modalSection = document.getElementById('halfModalBody');
    const modal = document.getElementById('halfModal');

    modalSection.innerHTML = content.innerHTML;
    content.innerHTML = '';

    modal.classList.remove('hide');
}

// 하프모달 닫기
function closeHalfModal(containerId) {
    document.getElementById('halfModal').classList.add('hide');

    const content = document.getElementById(containerId);
    const modalSection = document.getElementById('halfModalBody');
    const modal = document.getElementById('halfModal');

    modal.classList.add('hide');
    setTimeout(() => {
        content.innerHTML = modalSection.innerHTML;
        modalSection.innerHTML = '';
    }, 300);
}

function fetchContent(endPoint, payload = null) {

    // 로딩 표시
    const loadingEl = document.getElementById('loading');
    if (loadingEl) loadingEl.style.display = 'flex';

    // fetch 옵션
    const options = {
        method: payload ? 'POST' : 'GET',
        headers: {}
    };

    // payload가 있을 경우 FormData
    if (payload) {
        if (payload instanceof FormData) {
            options.body = payload; // 그대로 전송 (Content-Type 자동)
        } else if (typeof payload === 'object') {
            // 일반 JS 객체 → JSON
            options.body = JSON.stringify(payload);
            options.headers['Content-Type'] = 'application/json';
        }
    } else {
        // GET 요청인 경우 HTML
        options.headers['Content-Type'] = 'text/html';
    }

    return fetch(endPoint, options)
        .then(res => {
            // if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`);
            return res.text();
        })
        .then(html => {
            if (!html) throw new Error('Fetch returned empty content');
            
            // 로그인 페이지 HTML인지 확인 (로그인 페이지로 리다이렉트된 경우)
            if (html.includes('login-container') || html.includes('login-submit-btn') || 
                (html.includes('로그인') && html.includes('username') && html.includes('password'))) {
                
                // 항공편 상세 페이지인 경우 (payload가 있고 seatClassIds가 있는 경우)
                if (endPoint === '/air/detail' && payload && payload.seatClassIds) {
                    // 항공편 예약 정보를 세션에 저장
                    fetch('/api/save-air-reservation', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(payload)
                    }).catch(err => console.error('Failed to save air reservation:', err));
                } else {
                    // 일반 상품의 경우 원래 URL 저장 (하지만 사용하지 않음 - 홈으로 이동)
                    // fetch('/api/save-original-url', {
                    //     method: 'POST',
                    //     headers: { 'Content-Type': 'application/json' },
                    //     body: JSON.stringify({ url: endPoint })
                    // }).catch(err => console.error('Failed to save original URL:', err));
                }
            }
            
            return html;
        })
        .catch(err => {
            throw err;
        })
        .finally(() => {
            if (loadingEl) loadingEl.style.display = 'none'; // 로딩 숨김
        });
}

// fetchContent로 가져온 html의 script 실행
// -> 모든 DOM 요소 삽입 끝난 뒤 실행할것
function executeScripts(container) {
    if (!container) return;

    const scripts = Array.from(container.querySelectorAll('script'));

    scripts.forEach(script => {
        const s = document.createElement('script');
        if (script.src) {
            s.src = script.src;
            s.async = false; // 순차 로딩
        } else {
            s.textContent = script.textContent;
        }
        document.body.appendChild(s);
        document.body.removeChild(s);
    });
}

// URL 해시(#...) 감지 → 해당 섹션 표시
window.addEventListener('load', () => {
    const hash = location.hash.replace('#', '');
    const hashMap = {
        home: 1,
        menu: 2,
        search: 3,
        wish: 4,
        mypage: 5,
        dynamic: 6
    };
    if (hash && hashMap[hash]) {
        showSection(hashMap[hash]);
    } else {
        showSection(1); // 기타: 홈
    }
});


// 해시 변경시 자동 반응
window.addEventListener('hashchange', () => {
    const hash = location.hash.replace('#', '');
    const hashMap = {
        home: 1,
        menu: 2,
        search: 3,
        wish: 4,
        mypage: 5,
        dynamic: 6
    };
    if (hash && hashMap[hash]) {
        showSection(hashMap[hash]);
    }
});

// [TEST] 재귀적으로 무한 스택용 모달 content 생성
function getModalContent(depth) {
    return `
<div>
<p>모달 깊이: ${depth}</p>
<button onclick="addModal(getModalContent(${depth + 1}))">새 모달 추가</button>
<button onclick="addModal(getModalContent(${depth + 1}),true)">[Flush]새 모달 추가</button>
<button onclick="backModal()">뒤로</button>
</div>
`;
}

/* -------------------------------
   최근 검색 로딩
-------------------------------- */
function loadRecentSearches() {
    const listEl = document.querySelector('.recent-list');
    listEl.innerHTML = "";  // 초기화

    const data = JSON.parse(localStorage.getItem('recentSearches') || "[]");

    data.forEach(keyword => {
        // <li>
        const li = document.createElement('li');

        // <span class="keyword-text">키워드</span>
        const span = document.createElement('span');
        span.classList.add('keyword-text');
        span.textContent = keyword;

        // <i class="fas fa-times remove-icon"></i>
        const icon = document.createElement('i');
        icon.classList.add('fas', 'fa-times', 'remove-icon');

        // 검색어 클릭 시 검색 실행
        span.addEventListener('click', () => {
            const payload = { keyword };
            console.log("payload.keyword =", payload.keyword);
            openModal('/product/search', payload);
        });

        // 삭제 버튼
        icon.addEventListener('click', (e) => {
            e.stopPropagation();  // li 또는 span 클릭 이벤트 방지
            removeRecent(keyword);
        });

        // li 조립
        li.appendChild(span);
        li.appendChild(icon);

        // 리스트에 삽입
        listEl.appendChild(li);
    });
}


/* -------------------------------
   검색어 최근 목록 저장
-------------------------------- */
function saveRecentSearch(keyword) {
    if (!keyword.trim()) return;

    // 자동저장 OFF이면 저장하지 않음
    const auto = localStorage.getItem('searchAutoSave') || "on";
    if (auto === "off") return;

    let data = JSON.parse(localStorage.getItem('recentSearches') || "[]");

    data = data.filter(item => item !== keyword);
    data.unshift(keyword);
    if (data.length > 10) data.pop();

    localStorage.setItem('recentSearches', JSON.stringify(data));
}

/* -------------------------------
   특정 검색어 제거
-------------------------------- */
function removeRecent(keyword) {
    let data = JSON.parse(localStorage.getItem('recentSearches') || "[]");
    data = data.filter(item => item !== keyword);
    localStorage.setItem('recentSearches', JSON.stringify(data));
    loadRecentSearches();
}

/* -------------------------------
   전체 삭제
-------------------------------- */
function clearAllRecent() {
    localStorage.removeItem('recentSearches');
    loadRecentSearches();
}

/* -------------------------------
   검색 실행
-------------------------------- */
function initSearchForm() {
    const form = document.querySelector('#searchSection form');
    const input = form.querySelector('input[name="keyword"]');

    form.addEventListener('submit', function (e) {
        e.preventDefault();

        const keyword = input.value.trim();
        if (!keyword) return;

        // 최근 검색 저장
        saveRecentSearch(keyword);

        const payload = {
            keyword: keyword
        }
        console.log("payload" + payload.keyword);

        // 모달 열기
        openModal('/product/search', payload);

        // UI 갱신
        loadRecentSearches();
    });
}
/* ---------------------------------------
   자동저장 토글
--------------------------------------- */
function toggleAutoSave() {
    let status = localStorage.getItem('searchAutoSave') || "on";

    if (status === "on") {
        localStorage.setItem('searchAutoSave', 'off');
    } else {
        localStorage.setItem('searchAutoSave', 'on');
    }

    // UI 갱신
    updateAutoSaveText();
}
/* ---------------------------------------
   자동저장 버튼 텍스트 반영
--------------------------------------- */
function updateAutoSaveText() {
    const el = document.querySelector('.options span:last-child');

    const status = localStorage.getItem('searchAutoSave') || "on";

    if (status === "on") {
        el.textContent = "자동저장 끄기";
    } else {
        el.textContent = "자동저장 켜기";
    }
}
function clearSearchInput() {
    const input = document.querySelector('#searchSection input[name="keyword"]');
    if (!input) return;

    input.value = "";
    input.focus();
}