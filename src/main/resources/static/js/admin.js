const {createApp} = Vue;

createApp({
    data() {
        return {
            isAdminLoggedIn: false,
            adminUsername: '',
            adminPassword: '',
            whitelistUsers: [],
            newUsers: '',
            loading: false,
            message: '',
            messageType: 'success'
        }
    },
    computed: {
        selectedUserCount() {
            return this.whitelistUsers.filter(user => user.selected).length;
        },
        hasSelectedUsers() {
            return this.selectedUserCount > 0;
        },
        isAllSelected() {
            return this.whitelistUsers.length > 0 && this.whitelistUsers.every(user => user.selected);
        }
    },
    mounted() {
        this.checkAdminLogin();
    },
    methods: {
        async adminLogin() {
            if (!this.adminUsername || !this.adminPassword) {
                this.showMessage('请输入管理员用户名和密码', 'warning');
                return;
            }

            this.loading = true;
            try {
                const response = await axios.post('/admin/login', null, {
                    params: {
                        username: this.adminUsername,
                        password: this.adminPassword
                    }
                });

                let user = response.data.result;
                this.adminUsername = user.name;
                this.isAdminLoggedIn = true;
                this.showMessage('管理员登录成功', 'success');
                this.loadWhitelist();
            } catch (error) {
                this.showMessage(error.response?.message || '登录失败', 'danger');
            } finally {
                this.loading = false;
            }
        },

        async adminLogout() {
            await axios.get('/admin/logout');
            this.isAdminLoggedIn = false;
            this.adminUsername = '';
            this.adminPassword = '';
            this.whitelistUsers = [];
            this.newUsers = '';
            this.showMessage('管理员退出登录成功', 'success');
        },

        async loadWhitelist() {
            this.loading = true;
            try {
                const response = await axios.get('/admin/getWhitelist');
                this.whitelistUsers = response.data.result?.map(user => ({
                    value: user,
                    selected: false
                }));
            } catch (error) {
                this.showMessage('加载白名单失败', 'danger');
            } finally {
                this.loading = false;
            }
        },

        async addUsers() {
            if (!this.newUsers.trim()) {
                this.showMessage('请输入要添加的用户信息', 'warning');
                return;
            }

            this.loading = true;
            try {
                const response = await axios.post('/admin/addUsers', null, {params: {users: this.newUsers}});

                this.showMessage(`成功添加 ${response.data.result} 个用户到白名单`, 'success');
                this.newUsers = '';
                this.loadWhitelist();
            } catch (error) {
                this.showMessage(error.response?.data?.message || '添加用户失败', 'danger');
            } finally {
                this.loading = false;
            }
        },

        async removeSelectedUsers(usersToRemove) {
            // usersToRemove: 可选参数，若未传则取选中的
            let selectedUsers;
            if (usersToRemove && usersToRemove.length > 0) {
                selectedUsers = usersToRemove;
            } else {
                selectedUsers = this.whitelistUsers
                    .filter(user => user.selected)
                    .map(user => user.value);
            }

            if (selectedUsers.length === 0) {
                this.showMessage('请选择要删除的用户', 'warning');
                return;
            }

            if (!confirm(`确定要删除选中的 ${selectedUsers.length} 个用户吗？`)) {
                return;
            }

            this.loading = true;
            try {
                const response = await axios.post('/admin/removeUsers', null, {params: {users: selectedUsers.join(',')}});
                this.showMessage(`成功删除 ${response.data.result} 个用户`, 'success');
                this.loadWhitelist();
            } catch (error) {
                this.showMessage(error.response?.data?.message || '删除用户失败', 'danger');
            } finally {
                this.loading = false;
            }
        },

        async removeSingleUser(index) {
            // 直接调用批量删除逻辑
            const user = this.whitelistUsers[index];
            this.removeSelectedUsers([user.value]);
        },

        async refreshWhitelist() {
            this.loading = true;
            try {
                const response = await axios.get('/admin/refreshWhitelist');
                this.whitelistUsers = response.data.result.map(user => ({
                    value: user,
                    selected: false
                }));
                this.showMessage('白名单刷新成功', 'success');
            } catch (error) {
                this.showMessage('刷新白名单失败', 'danger');
            } finally {
                this.loading = false;
            }
        },

        toggleSelectAll() {
            const newState = !this.isAllSelected;
            this.whitelistUsers.forEach(user => {
                user.selected = newState;
            });
        },

        async checkAdminLogin() {
            try {
                const response = await axios.get('/admin/getCurrentAdmin');
                this.isAdminLoggedIn = true;
                // 新增：保存管理员用户名
                this.adminUsername = response.data.result.name;
                this.loadWhitelist();
            } catch (error) {
                // 管理员未登录，保持登录状态为false
                this.adminUsername = '';
                console.log("管理员未登录");
            }
        },

        showMessage(message, type = 'success') {
            showGlobalMessage(message, type);
        }
    }
}).mount('#admin');
