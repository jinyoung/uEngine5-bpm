<template>
  <div>
    <router-view></router-view>

    <!--서비스 로케이터 리스트-->
    <div v-if="profile == 'local'">
      <service-locator :host="location.protocol + '//' + location.hostname + ':8080'" path="/"
                       resource-name="codi"></service-locator>

      <service-locator ref="backend" :host="location.protocol + '//' + location.hostname + ':8080'"></service-locator>
    </div>
    <div v-else>
      <service-locator :host="'http://' + config.vcap.services['uengine5-router'][profile].external" path="/"
                       resource-name="codi"></service-locator>

      <service-locator ref="backend"
                       :host="'http://' + config.vcap.services['uengine5-router'][profile].external"></service-locator>
    </div>

    <!--글로벌 알림 컴포넌트-->
    <md-snackbar md-position="top right" ref="snackbar" :md-duration="4000">
      <span class="md-primary">{{snackbar.text}}</span>
      <md-button class="md-accent" md-theme="light-blue" @click="$refs.snackbar.close()">Close</md-button>
    </md-snackbar>
  </div>
</template>
<script>
    export default {
        data() {
            return {
                config: window.config,
                profile: profile,
                location: window.location,
                snackbar: {
                    top: true,
                    right: true,
                    timeout: 6000,
                    trigger: false,
                    mode: 'multi-line',
                    context: 'info',
                    text: ''
                },
            }
        },
        created() {
            if(!localStorage.getItem('accessToken')){
                window.location = '/api/accessToken/'  //check valid accessToken
            }
        },
        mounted() {


        },
        methods: {
            info: function (msg) {
                this.snackbar.context = 'info';
                this.snackbar.text = msg;
                this.$refs.snackbar.open();
            },
            error: function (msg) {
                this.snackbar.context = 'error';
                this.snackbar.text = msg;
                this.$refs.snackbar.open();
            },
            warning: function (msg) {
                this.snackbar.context = 'warning';
                this.snackbar.text = msg;
                this.$refs.snackbar.open();
            },
            success: function (msg) {
                this.snackbar.context = 'success';
                this.snackbar.text = msg;
                this.$refs.snackbar.open();
            }
        }
    }
</script>

<style lang="scss" rel="stylesheet/scss">
  @import '/admin/static/bpmn/css/custom.css';
</style>
