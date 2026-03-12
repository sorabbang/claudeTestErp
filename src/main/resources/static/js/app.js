// ERP Application JavaScript

document.addEventListener('DOMContentLoaded', function () {
    // Auto-dismiss alerts after 3 seconds
    document.querySelectorAll('.alert-dismissible').forEach(function (alert) {
        setTimeout(function () {
            var bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
            bsAlert.close();
        }, 3000);
    });

    // Sidebar toggle
    var sidebarToggle = document.getElementById('sidebarToggle');
    if (sidebarToggle) {
        sidebarToggle.addEventListener('click', function () {
            document.body.classList.toggle('sidebar-collapsed');
            var sidebar = document.getElementById('sidebar');
            if (window.innerWidth <= 992) {
                sidebar.classList.toggle('show');
            }
        });
    }

    // Current date display
    var currentDateEl = document.getElementById('currentDate');
    if (currentDateEl) {
        var now = new Date();
        var options = { year: 'numeric', month: 'long', day: 'numeric', weekday: 'short' };
        currentDateEl.textContent = now.toLocaleDateString('ko-KR', options);
    }

    // Menu search
    var menuSearch = document.getElementById('menuSearch');
    if (menuSearch) {
        menuSearch.addEventListener('input', function () {
            var keyword = this.value.toLowerCase().trim();
            document.querySelectorAll('.sidebar .nav-item').forEach(function (item) {
                var link = item.querySelector('.nav-link');
                if (!link) return;
                var text = link.textContent.toLowerCase();
                item.style.display = keyword === '' || text.includes(keyword) ? '' : 'none';
            });
        });
    }
});

// ========== Chatbot ==========
(function() {
    var fab = document.getElementById('chatbotFab');
    var win = document.getElementById('chatbotWindow');
    var closeBtn = document.getElementById('chatbotClose');
    var input = document.getElementById('chatInput');
    var sendBtn = document.getElementById('chatSend');
    var body = document.getElementById('chatbotBody');

    if (!fab || !win) return;

    // FAQ 데이터베이스
    var faqData = [
        {
            keywords: ['대시보드', '메인', '홈', '첫화면', '현황'],
            answer: '대시보드는 ERP 시스템의 메인 화면입니다.\n\n' +
                '- 계정과목/전표 건수 요약\n' +
                '- 이번달 차변 합계 (상태별)\n' +
                '- 월별 전표 현황 차트\n' +
                '- 최근 전표 목록\n\n' +
                '좌측 메뉴에서 "대시보드"를 클릭하거나 로고를 클릭하면 이동합니다.'
        },
        {
            keywords: ['계정과목', '계정', '과목', '자산', '부채', '자본', '수익', '비용'],
            answer: '계정과목 관리에서는 회계 계정을 등록/수정/삭제할 수 있습니다.\n\n' +
                '- 유형: 자산, 부채, 자본, 수익, 비용\n' +
                '- 코드/이름/설명/활성여부 설정\n' +
                '- 유형별 그룹화하여 아코디언으로 표시\n\n' +
                '메뉴: 회계/재무 > 계정과목 관리'
        },
        {
            keywords: ['전표', '입력', '등록', '작성', '분개'],
            answer: '전표 입력 방법:\n\n' +
                '1. 전표 관리 > "신규 등록" 클릭\n' +
                '2. 거래일자, 적요 입력\n' +
                '3. 분개 항목 추가 (차변/대변)\n' +
                '4. 차변 합계 = 대변 합계가 되어야 저장 가능\n' +
                '5. "저장" 버튼 클릭\n\n' +
                '메뉴: 회계/재무 > 전표 관리 > 신규 등록'
        },
        {
            keywords: ['상태', '승인', '전기', '취소', '임시저장', '워크플로우'],
            answer: '전표 상태 흐름:\n\n' +
                '임시저장(DRAFT) → 승인(APPROVED) → 전기완료(POSTED)\n\n' +
                '- 임시저장: 수정/삭제 가능\n' +
                '- 승인: 전기 처리 가능\n' +
                '- 전기완료: 최종 확정, 수정 불가\n' +
                '- 취소: 전기 전 단계에서 가능\n\n' +
                '전표 상세 화면에서 상태 변경 버튼을 사용하세요.'
        },
        {
            keywords: ['차변', '대변', '합계', '금액', '잔액'],
            answer: '차변/대변 규칙:\n\n' +
                '- 차변(Debit): 자산 증가, 비용 발생\n' +
                '- 대변(Credit): 부채/자본 증가, 수익 발생\n' +
                '- 전표 저장 시 차변 합계 = 대변 합계 필수\n\n' +
                '대시보드에서 이번달 차변 합계를 상태별로 확인할 수 있습니다.'
        },
        {
            keywords: ['디버그', '콘솔', 'SQL', '쿼리', '데이터베이스', 'DB'],
            answer: '디버그 콘솔 기능:\n\n' +
                '- 시스템 정보 (Java, JVM, OS)\n' +
                '- JVM 메모리 모니터링\n' +
                '- DB 테이블/데이터 현황\n' +
                '- SQL 쿼리 실행 (SELECT 전용)\n' +
                '- GC 수동 실행\n\n' +
                '메뉴: 시스템 > 디버그 콘솔'
        },
        {
            keywords: ['수정', '편집', '변경'],
            answer: '데이터 수정 방법:\n\n' +
                '계정과목: 목록에서 수정 버튼(연필 아이콘) 클릭\n' +
                '전표: 임시저장(DRAFT) 상태에서만 수정 가능\n' +
                '  → 전표 상세 > 수정 버튼 클릭\n\n' +
                '승인/전기 완료된 전표는 수정할 수 없습니다.'
        },
        {
            keywords: ['삭제', '제거'],
            answer: '데이터 삭제:\n\n' +
                '계정과목: 목록에서 삭제 버튼(휴지통 아이콘) 클릭\n' +
                '전표: 임시저장(DRAFT) 상태에서만 삭제 가능\n' +
                '  → 전표 상세 > 삭제 버튼 클릭\n\n' +
                '삭제는 되돌릴 수 없으니 주의하세요!'
        },
        {
            keywords: ['검색', '찾기'],
            answer: '검색 기능:\n\n' +
                '- 상단 네비게이션 바의 검색창으로 메뉴를 검색할 수 있습니다.\n' +
                '- 메뉴 이름을 입력하면 사이드바에서 해당 메뉴만 필터링됩니다.'
        },
        {
            keywords: ['H2', '콘솔', '데이터베이스'],
            answer: 'H2 데이터베이스 콘솔:\n\n' +
                'URL: /h2-console\n' +
                'JDBC URL: jdbc:h2:mem:erpdb\n' +
                'Username: sa\n' +
                'Password: (비어있음)\n\n' +
                '디버그 콘솔에서 "H2 Console" 버튼으로도 접근 가능합니다.'
        }
    ];

    var defaultSuggestions = ['전표 입력 방법', '계정과목이란?', '상태 변경', '대시보드 기능', '디버그 콘솔'];

    function addMessage(text, type) {
        var msgDiv = document.createElement('div');
        msgDiv.className = 'chat-message ' + type;

        var avatar = document.createElement('div');
        avatar.className = 'chat-avatar';
        avatar.innerHTML = type === 'bot' ? '<i class="bi bi-robot"></i>' : '<i class="bi bi-person"></i>';

        var bubble = document.createElement('div');
        bubble.className = 'chat-bubble';
        bubble.innerHTML = text.replace(/\n/g, '<br>');

        msgDiv.appendChild(avatar);
        msgDiv.appendChild(bubble);
        body.appendChild(msgDiv);

        body.scrollTop = body.scrollHeight;
    }

    function addSuggestions(suggestions) {
        var msgDiv = document.createElement('div');
        msgDiv.className = 'chat-message bot';

        var avatar = document.createElement('div');
        avatar.className = 'chat-avatar';
        avatar.style.visibility = 'hidden';

        var sugDiv = document.createElement('div');
        sugDiv.className = 'chat-suggestions';
        suggestions.forEach(function(s) {
            var btn = document.createElement('button');
            btn.className = 'chat-suggestion-btn';
            btn.textContent = s;
            btn.addEventListener('click', function() {
                processInput(s);
            });
            sugDiv.appendChild(btn);
        });

        msgDiv.appendChild(avatar);
        msgDiv.appendChild(sugDiv);
        body.appendChild(msgDiv);
        body.scrollTop = body.scrollHeight;
    }

    function findAnswer(question) {
        var q = question.toLowerCase();
        var bestMatch = null;
        var bestScore = 0;

        for (var i = 0; i < faqData.length; i++) {
            var score = 0;
            for (var j = 0; j < faqData[i].keywords.length; j++) {
                if (q.indexOf(faqData[i].keywords[j]) !== -1) {
                    score++;
                }
            }
            if (score > bestScore) {
                bestScore = score;
                bestMatch = faqData[i];
            }
        }

        if (bestMatch && bestScore > 0) {
            return bestMatch.answer;
        }

        return '죄송합니다. 해당 질문에 대한 답변을 찾지 못했습니다.\n\n' +
            '아래 주제에 대해 질문해 보세요:\n' +
            '- 대시보드 / 메인화면\n' +
            '- 계정과목 관리\n' +
            '- 전표 입력/수정/삭제\n' +
            '- 상태 변경 (승인/전기/취소)\n' +
            '- 차변/대변 규칙\n' +
            '- 디버그 콘솔 / SQL\n' +
            '- H2 데이터베이스';
    }

    function processInput(text) {
        addMessage(text, 'user');
        input.value = '';

        setTimeout(function() {
            var answer = findAnswer(text);
            addMessage(answer, 'bot');
        }, 400);
    }

    // 초기 메시지
    function initChat() {
        body.innerHTML = '';
        addMessage('안녕하세요! ERP 도우미입니다.\n메뉴 사용법이나 기능에 대해 궁금한 점을 물어보세요.', 'bot');
        addSuggestions(defaultSuggestions);
    }

    // 이벤트
    fab.addEventListener('click', function() {
        var isOpen = win.classList.contains('show');
        if (isOpen) {
            win.classList.remove('show');
            fab.classList.remove('active');
            fab.innerHTML = '<i class="bi bi-chat-dots-fill"></i>';
        } else {
            win.classList.add('show');
            fab.classList.add('active');
            fab.innerHTML = '<i class="bi bi-x-lg"></i>';
            if (body.children.length === 0) initChat();
            input.focus();
        }
    });

    closeBtn.addEventListener('click', function() {
        win.classList.remove('show');
        fab.classList.remove('active');
        fab.innerHTML = '<i class="bi bi-chat-dots-fill"></i>';
    });

    sendBtn.addEventListener('click', function() {
        var text = input.value.trim();
        if (text) processInput(text);
    });

    input.addEventListener('keydown', function(e) {
        if (e.key === 'Enter') {
            var text = input.value.trim();
            if (text) processInput(text);
        }
    });
})();

// Dashboard Charts
function initDashboardCharts(accountTypeData, monthlyData) {
    if (!accountTypeData || !monthlyData) return;

    // Account Type Doughnut Chart
    var pieCtx = document.getElementById('accountTypeChart');
    if (pieCtx) {
        var labels = Object.keys(accountTypeData);
        var data = Object.values(accountTypeData);
        new Chart(pieCtx, {
            type: 'doughnut',
            data: {
                labels: labels,
                datasets: [{
                    data: data,
                    backgroundColor: [
                        '#3b82f6', '#ef4444', '#8b5cf6', '#10b981', '#f59e0b'
                    ],
                    borderWidth: 2,
                    borderColor: '#fff'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            padding: 15,
                            usePointStyle: true,
                            pointStyleWidth: 10,
                            font: { size: 11 }
                        }
                    }
                },
                cutout: '55%'
            }
        });
    }

    // Monthly Bar/Line Chart
    var barCtx = document.getElementById('monthlyChart');
    if (barCtx) {
        var mLabels = Object.keys(monthlyData);
        var mData = Object.values(monthlyData);
        var monthlyChart = new Chart(barCtx, {
            type: 'bar',
            data: {
                labels: mLabels,
                datasets: [{
                    label: '전표 건수',
                    data: mData,
                    backgroundColor: 'rgba(59, 130, 246, 0.7)',
                    borderColor: '#3b82f6',
                    borderWidth: 1,
                    borderRadius: 6,
                    borderSkipped: false
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            stepSize: 1,
                            font: { size: 11 }
                        },
                        grid: { color: 'rgba(0,0,0,0.05)' }
                    },
                    x: {
                        ticks: { font: { size: 11 } },
                        grid: { display: false }
                    }
                }
            }
        });

        // Chart type toggle (bar <-> line)
        document.querySelectorAll('[data-chart]').forEach(function (btn) {
            btn.addEventListener('click', function () {
                document.querySelectorAll('[data-chart]').forEach(function (b) {
                    b.classList.remove('active');
                });
                this.classList.add('active');
                monthlyChart.config.type = this.dataset.chart;
                if (this.dataset.chart === 'line') {
                    monthlyChart.data.datasets[0].borderWidth = 2;
                    monthlyChart.data.datasets[0].fill = true;
                    monthlyChart.data.datasets[0].backgroundColor = 'rgba(59, 130, 246, 0.1)';
                    monthlyChart.data.datasets[0].tension = 0.3;
                    monthlyChart.data.datasets[0].pointBackgroundColor = '#3b82f6';
                } else {
                    monthlyChart.data.datasets[0].borderWidth = 1;
                    monthlyChart.data.datasets[0].fill = false;
                    monthlyChart.data.datasets[0].backgroundColor = 'rgba(59, 130, 246, 0.7)';
                    monthlyChart.data.datasets[0].tension = 0;
                }
                monthlyChart.update();
            });
        });
    }
}
