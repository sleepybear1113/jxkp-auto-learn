// 全局消息显示函数
function showGlobalMessage(text, type = 'danger') {
    // 检查是否存在Vue应用实例
    if (window.app && window.app.showMessage) {
        window.app.showMessage(text, type);
    } else {
        // 如果Vue应用不存在，创建一个临时的消息显示
        const messageDiv = document.createElement('div');
        messageDiv.style.cssText = `
            position: fixed;
            top: 20px;
            left: 50%;
            transform: translateX(-50%);
            z-index: 1000;
            min-width: 300px;
            padding: 15px;
            border-radius: 8px;
            color: white;
            font-weight: 500;
            text-align: center;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
        `;
        
        if (type === 'success') {
            messageDiv.style.background = '#28a745';
        } else if (type === 'warning') {
            messageDiv.style.background = '#ffc107';
            messageDiv.style.color = '#212529';
        } else {
            messageDiv.style.background = '#dc3545';
        }
        
        messageDiv.textContent = text;
        document.body.appendChild(messageDiv);
        
        setTimeout(() => {
            if (messageDiv.parentNode) {
                messageDiv.parentNode.removeChild(messageDiv);
            }
        }, 3000);
    }
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
