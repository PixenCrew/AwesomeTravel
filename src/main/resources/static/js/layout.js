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

document.addEventListener('DOMContentLoaded', () => {
    loadRecentSearches();
    initSearchForm();
    updateAutoSaveText();
});

function showSection(sectionIndex, push = true) {
    if (currentSection === sectionIndex) return;
    if (isAnimating) return;

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

    // 애니메이팅 완료 처리
    setTimeout(() => {
        startSection.classList.remove('exit');
        isAnimating = false;
    }, 200);

    currentSection = sectionIndex;
}

function fetchSection(endPoint, payload = null) {
    fetchContent(endPoint, payload).then(html => {
        // const dynamicSection = document.getElementById('dynamicSection');

        // if (currentSection !== 6) {
        //     showSection(6);
        //     dynamicSection.innerHTML = html;
        // } else {
        //     // 기존 다이내믹 섹션 기록
        //     sectionStack.push({
        //         type: 'dynamic',
        //         content: dynamicSection.innerHTML
        //     });
        //     if (sectionStack.length > sectionNavMaxLength) sectionStack.shift();

        //     dynamicSection.innerHTML = html;

        //     executeScripts(dynamicSection);
        // }
        const container = document.getElementById('dynamicSection');

        // 새 섹션 div 생성
        const newSection = document.createElement('div');
        newSection.classList.add('dynamic-section-new');
        newSection.innerHTML = html;

        // 기존 내용 스택에 저장
        if (currentSection === 6) {
            sectionStack.push({
                type: 'dynamic',
                content: container.innerHTML
            });
            if (sectionStack.length > sectionNavMaxLength) sectionStack.shift();
        }

        // 기존 dynamicSection 내용 교체
        container.innerHTML = ''; // 기존 내용 제거
        container.appendChild(newSection);

        // 스크립트 실행
        executeScripts(newSection);

        // 현재 섹션 표시
        if (currentSection !== 6) showSection(6);
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
        document.getElementById('dynamicSection').innerHTML = prev.content;

        // 모든 section 상태 초기화 후 dynamicSection만 active
        document.getElementById('dynamicSection').classList.add('active');
        currentSection = 6;
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

        console.log("refreshSectionsAfterLogin2");

    } catch (err) {
        console.error("갱신 실패:", err);
    }
}

// ===========================섹션 - 메뉴 탭=================
function showMenuTab(tabId) {
    const tabs = ['menu-travel', 'menu-support'];

    tabs.forEach(id => {
        const menuElement = document.getElementById(id);
        if (!menuElement) return;

        if (id === tabId) {
            menuElement.classList.add('active');
        } else {
            menuElement.classList.remove('active');
        }
    });
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

    // 새 모달 DOM 생성
    const newModal = document.createElement('div');
    newModal.classList.add('modal-slide', 'show');
    fetchContent(endPoint, payload).then(html => {
        newModal.innerHTML = html
        executeScripts(newModal);
    }); // 모달 내용물 fetch
    modalBody.appendChild(newModal);
    currentModal = newModal;
}

// 문자열 모달 열기
function openModalHtml(htmlString) {
    modalStack = [];
    fullModal.classList.remove('hide');
    fullModal.classList.add('show');

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