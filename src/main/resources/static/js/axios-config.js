// 全局消息显示函数
function showGlobalMessage(text, type = 'danger') {
    // 消息容器id
    const containerId = 'global-message-container';
    let container = document.getElementById(containerId);
    if (!container) {
        container = document.createElement('div');
        container.id = containerId;
        container.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 10000;
            display: flex;
            flex-direction: column;
            align-items: flex-end;
            pointer-events: none;
        `;
        document.body.appendChild(container);
    }

    // 创建消息div
    const messageDiv = document.createElement('div');
    messageDiv.style.cssText = `
        margin-top: 10px;
        min-width: 300px;
        max-width: 400px;
        padding: 15px 24px;
        border-radius: 8px;
        color: white;
        font-weight: 500;
        text-align: center;
        box-shadow: 0 4px 12px rgba(0,0,0,0.12);
        background: #dc3545;
        opacity: 0.97;
        pointer-events: auto;
        transition: opacity 0.3s;
    `;
    if (type === 'success') {
        messageDiv.style.background = '#28a745';
    } else if (type === 'warning') {
        messageDiv.style.background = '#ffc107';
        messageDiv.style.color = '#212529';
    }
    messageDiv.textContent = text;

    // 添加到容器
    container.appendChild(messageDiv);

    // 最多显示8个，超出移除最早的
    while (container.children.length > 8) {
        container.removeChild(container.firstChild);
    }

    // 3秒后自动消失
    setTimeout(() => {
        messageDiv.style.opacity = '0';
        setTimeout(() => {
            if (messageDiv.parentNode) {
                messageDiv.parentNode.removeChild(messageDiv);
            }
            // 如果容器空了，移除容器
            if (container.children.length === 0 && container.parentNode) {
                container.parentNode.removeChild(container);
            }
        }, 300);
    }, 3000);
}

// axios 全局拦截器
axios.interceptors.response.use(
    // 如果返回的状态码为 200，说明接口请求成功，可以正常拿到数据
    (response) => {
        let status = response.status;
        if (status === 200) {
            let res = response.data;
            let code = res.code;
            if (code === 0) {
                return response;
            } else {
                let message = res.message;
                if (code > -10) {
                    showGlobalMessage(message, 'danger');
                    return Promise.reject(response);
                } else if (code) {
                    showGlobalMessage("系统错误:" + message, 'danger');
                    // 直接拒绝往下面返回结果信息
                    return Promise.reject(response);
                }
                return response;
            }
        }
        return response;
    },
    // 否则的话抛出错误
    (error) => {
        let response = error.response;
        if (response == null) {
            showGlobalMessage(`请求失败，请检查程序是否启动`, 'danger');
            return Promise.reject(error);
        }
        let status = response.status;
        // 401 到 index.html
        if (status === 401) {
            window.location.href = "/index.html";
            return Promise.reject(error);
        }
        if (status >= 500) {
            showGlobalMessage(`[${status}]服务器发生错误，无法处理！`, 'danger');
            return Promise.reject(error);
        }

        let request = error.request;
        showGlobalMessage(`请求失败，code = ${status}`, 'danger');
        return Promise.reject(error);
    }
);
axios.interceptors.request.use(
    config => {
        if (config.method === "get") {
            config.params = {
                ...config.params,
                _t: new Date().getTime().toString().substring(6, 13),
            };
        }
        config.headers["Request-From"] = "axios";
        return config
    }, function (error) {
        return Promise.reject(error);
    },
);

axios.defaults.baseURL = "/api-jxkp-auto-learn";
