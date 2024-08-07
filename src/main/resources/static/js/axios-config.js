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
                    alert(message);
                    return Promise.reject(response);
                } else if (code) {
                    alert("系统错误:" + message);
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
            alert(`请求失败，请检查程序是否启动`);
            return Promise.reject(error);
        }
        let status = response.status;
        // 401 到 index.html
        if (status === 401) {
            window.location.href = "/index.html";
            return Promise.reject(error);
        }
        if (status >= 500) {
            alert(`[${status}]服务器发生错误，无法处理！\n${request.responseURL}`);
            return Promise.reject(error);
        }

        let request = error.request;
        alert(`请求失败，code = ${status}\n url：${request.responseURL}`);
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
