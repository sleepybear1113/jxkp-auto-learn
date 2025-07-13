const {createApp} = Vue;
let app = createApp({
    data() {
        return {
            tab: 1,
            userCookie: "",
            idCard: "",
            idCard1: "",
            password: "",
            password1: "",
            captcha: "",
            loadCaptcha: false,
            captchaImgSrc: "",
            userInfoDto: new UserInfoDto(),
            courseInfoDtoList: [],
            showLogin: false,
            t: new Date().getTime(),
            loading: false,
            message: "",
            messageType: "",
        };
    },
    created() {
        this.getUserProfile();
    },
    methods: {
        showMessage(text, type = 'success') {
            this.message = text;
            this.messageType = type;
            setTimeout(() => {
                this.message = "";
            }, 3000);
        },
        
        login() {
            // 详细的表单验证
            let missingFields = [];
            if (!this.idCard || !this.idCard.trim()) {
                missingFields.push("用户名");
            }
            if (!this.password || !this.password.trim()) {
                missingFields.push("密码");
            }
            if (!this.captcha || !this.captcha.trim()) {
                missingFields.push("验证码");
            }
            
            if (missingFields.length > 0) {
                this.showMessage(`请填写：${missingFields.join("、")}`, "danger");
                return;
            }
            
            this.loading = true;
            let url = "/login";
            let loginParams = {
                params: {
                    idCard: this.idCard,
                    password: this.password,
                    captcha: this.captcha,
                }
            };
            axios.get(url, loginParams).then(res => {
                let data = res.data.result;
                this.userInfoDto = new UserInfoDto(data);
                this.showMessage("登录成功！请点击\"获取所有课程\"");
                this.tab = 3;
            }).catch((error) => {
                this.showMessage("登录失败，请检查信息是否正确", "danger");
            }).finally(() => {
                this.loading = false;
            });
        },
        
        addUserCookie() {
            if (!this.userCookie || !this.userCookie.trim()) {
                this.showMessage("请填写：Cookie", "danger");
                return;
            }
            
            this.loading = true;
            let url = "/addUserCookie";
            let addUserCookieParams = {
                params: {
                    cookie: this.userCookie.replaceAll("'", ""),
                }
            };
            axios.get(url, addUserCookieParams).then(res => {
                let data = res.data.result;
                this.userInfoDto = new UserInfoDto(data);
                this.showMessage("Cookie登录成功！");
                console.log(this.userInfoDto);
            }).catch((error) => {
                this.showMessage("Cookie登录失败，请检查Cookie是否有效", "danger");
            }).finally(() => {
                this.loading = false;
            });
        },
        
        getCaptchaImg() {
            let url = "/getCaptchaImg";
            return `${url}?t=${new Date().getTime()}&idCard=${this.idCard}&password=${this.password}`;
        },
        
        getCaptcha() {
            if (!this.idCard || !this.password) {
                this.showMessage("请输入账号和密码！", "danger");
                return;
            }
            this.loadCaptcha = false;
            this.t = new Date().getTime();
            this.idCard1 = this.idCard;
            this.password1 = this.password;
            setTimeout(() => this.loadCaptcha = true, 500);
            this.showMessage("验证码已刷新", "warning");
        },
        
        getUserProfile() {
            this.loading = true;
            let url = "/getUserProfile";
            axios.get(url).then(res => {
                let data = res.data.result;
                this.userInfoDto = new UserInfoDto(data);
                if (this.userInfoDto.name) {
                    this.showMessage("用户信息获取成功");
                }
                console.log(this.userInfoDto);
            }).catch((error) => {
                this.showMessage("获取用户信息失败", "danger");
            }).finally(() => {
                this.loading = false;
            });
        },
        
        getPersonTotalLessons() {
            this.loading = true;
            let url = "/getPersonTotalLessons";
            axios.get(url).then(res => {
                let data = res.data.result;
                this.courseInfoDtoList = mapToArray(data, CourseInfoDto);
                this.showMessage(`成功获取 ${this.courseInfoDtoList.length} 门课程`);
                console.log(this.courseInfoDtoList);
            }).catch((error) => {
                this.showMessage("获取课程列表失败", "danger");
            }).finally(() => {
                this.loading = false;
            });
        },
        
        learn() {
            let kcIdList = [];
            this.courseInfoDtoList.forEach(item => {
                if (item.status === "已学完") {
                    return;
                }
                if (item.checked) {
                    kcIdList.push(item.kcId);
                }
            });

            if (kcIdList.length === 0) {
                this.showMessage("请选择要学习的课程！", "danger");
                return;
            }

            this.loading = true;
            let url = "/learn";
            let learnParams = {
                params: {
                    kcIdList: kcIdList.join(","),
                }
            };
            axios.get(url, learnParams).then(res => {
                this.showMessage("开始学习中，请耐心等待...", "success");
                console.log(res.data.result);
            }).catch((error) => {
                this.showMessage("开始学习失败", "danger");
            }).finally(() => {
                this.loading = false;
            });
        },
        
        stop() {
            this.loading = true;
            let url = "/stop";
            axios.get(url).then(res => {
                this.showMessage("正在停止学习中，请稍后...", "warning");
            }).catch((error) => {
                this.showMessage("停止学习失败", "danger");
            }).finally(() => {
                this.loading = false;
            });
        },
        
        getRandom() {
            return Math.random();
        },
    },
    watch: {}
});
app.mount("#app");