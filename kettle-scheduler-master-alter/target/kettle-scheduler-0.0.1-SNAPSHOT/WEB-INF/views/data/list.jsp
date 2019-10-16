<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/";%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <base href="<%=basePath %>">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>数据统计</title>

    <link href="static/css/bootstrap.min.css?v=3.3.6" rel="stylesheet">
    <link href="static/css/font-awesome.css?v=4.4.0" rel="stylesheet">
    <link href="static/css/plugins/bootstrap-table/bootstrap-table.min.css" rel="stylesheet">
    <link href="static/css/animate.css" rel="stylesheet">
    <link href="static/css/style.css?v=4.1.0" rel="stylesheet">

</head>
<body class="gray-bg">
<div class="wrapper wrapper-content animated fadeInRight">
    <div class="row">
        <div class="col-sm-4">
            <div class="widget style1 navy-bg">
                <div class="row">
                    <div class="col-sm-4" style="opacity:0.2">
                        <i class="fa fa-exchange fa-5x" aria-hidden="true"></i>
                    </div>
                    <div class="col-sm-8 text-right" style="font-size:20px">
                        <span> 总表数 </span>
                        <h2 class="font-bold" id="allTb"></h2>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-sm-4">
            <div class="widget style1 navy-bg">
                <div class="row">
                    <div class="col-sm-4" style="opacity:0.2">
                        <i class="fa fa-check-circle-o fa-5x" aria-hidden="true"></i>
                    </div>
                    <div class="col-sm-8 text-right" style="font-size:20px">
                        <span> 正常个数 </span>
                        <h2 class="font-bold" id="allSuccess"></h2>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-sm-4">
            <div class="widget style1 yellow-bg">
                <div class="row">
                    <div class="col-sm-4" style="opacity:0.2">
                        <i class="fa fa-times-circle-o fa-5x" aria-hidden="true"></i>
                    </div>
                    <div class="col-sm-8 text-right" style="font-size:20px">
                        <span> 异常个数 </span>
                        <h2 class="font-bold" id="allEx"></h2>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="ibox float-e-margins">
        <div class="ibox-title">
            <span class="col-sm-5">数据统计</span>
        </div>
        <div class="col-sm-5"></div>
        <div class="ibox-content">
            <table id="userList" data-toggle="table"
                   data-url="data/getList.shtml"
                   data-query-params=queryParams data-query-params-type="limit"
                   data-pagination="true"
                   data-side-pagination="server" data-pagination-loop="false" >
                <thead>
                <tr id="tbRow">
                    <th data-field="id">表编号</th>
                    <th data-field="tbCnname">表中文名</th>
                    <th data-field="tbEnname">表英文名</th>
                    <th data-field="tbMaxTime">表最大时间</th>
                    <th data-field="lastUpdateTime">统计时间</th>
                    <th data-field="tbCount">数据量</th>
                    <th data-field="status" data-formatter="recordStatusFormatter">表状态</th>
                    <th data-field="updateTime" >上次表最大时间更新距今</th>
                </tr>
                </thead>
            </table>
        </div>
    </div>
</div>
<!-- 全局js -->
<script src="static/js/jquery.min.js?v=2.1.4"></script>
<script src="static/js/bootstrap.min.js?v=3.3.6"></script>
<!-- layer javascript -->
<script src="static/js/plugins/layer/layer.min.js"></script>
<!-- 自定义js -->
<script src="static/js/content.js?v=1.0.0"></script>
<!-- Bootstrap table -->
<script src="static/js/plugins/bootstrap-table/bootstrap-table.min.js"></script>
<script src="static/js/plugins/bootstrap-table/bootstrap-table-mobile.min.js"></script>
<script src="static/js/plugins/bootstrap-table/locale/bootstrap-table-zh-CN.min.js"></script>
<script>
    $(document).ready(function () {
        $.ajax({
            type: 'POST',
            async: false,
            url: 'data/getAllTb.shtml',
            data: {},
            success: function (data) {
                console.log(data);
                $("#allTb").text(data);
            },
            error: function () {
                alert("请求失败！请刷新页面重试");
            },
            dataType: 'json'
        });
        $.ajax({
            type: 'POST',
            async: false,
            url: 'data/getAllEx.shtml',
            data: {},
            success: function (data) {
                console.log(data);
                $("#allEx").text(data);
            },
            error: function () {
                alert("请求失败！请刷新页面重试");
            },
            dataType: 'json'
        });
        $.ajax({
            type: 'POST',
            async: false,
            url: 'data/getAllSuccess.shtml',
            data: {},
            success: function (data) {
                console.log(data);
                $("#allSuccess").text(data);
            },
            error: function () {
                alert("请求失败！请刷新页面重试");
            },
            dataType: 'json'
        });

    });

    $('#userList').on('load-success.bs.table', function () {
        //alert($('#userList').html());
        $("table tbody tr").each(function(){
            $(this).find('td').each(function(row) {
                if($(this).html().indexOf("秒")=== -1){
                    return $(this).siblings().eq(3).attr("style","color:red");
                }
            });
        });

    });

    function recordStatusFormatter(value, row, index) {

        if (value == "1") {
            return "运行正常";
        } else if (value == "2") {
            return "运行异常";
        } else {
            return "未定义";
        }
    };


    function queryParams(params) {
        var temp = {limit: 10, offset: params.offset};
        return temp;
    };

</script>
</body>
</html>