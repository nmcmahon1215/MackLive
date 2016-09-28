/**
 * Initialization for the live blog feed
 */
var liveFeed;
var commentController;

$j(document).ready(function () {
    var query = {}, hash;
    var q = document.URL.split('?')[1];
    if (q != undefined) {
        q = q.split('&');
        for (var i = 0; i < q.length; i++) {
            hash = q[i].split('=');
            query[hash[0]] = hash[1];
        }
    }

    var gameId = query['gameId'];
    if (!gameId) {
        alert("No game specified");
    }


    liveFeed = document.getElementById("liveBlogFeed");
    liveFeed.src = "/client/liveBlogFeed.html?gameId=" + gameId;

    commentController = new ClientLiveFeed(gameId);
    commentController.render();
    commentController.initialize();
});
