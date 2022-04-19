package jp.techacademy.hide.yui.taskapp

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.app.PendingIntent
import android.graphics.BitmapFactory
import android.app.NotificationManager
import android.app.NotificationChannel
import android.os.Build
import androidx.core.app.NotificationCompat
import io.realm.Realm

class TaskAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        Log.d("TaskApp", "onReceive")

        // 通知はNotificationクラスを作成して、NotificationManagerにセットすることで表示できる
        val notificationManager = context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // SDKバージョンが26以上の場合、チャネルを設定する必要がある
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel("default",
                "Channel name",
                NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = "Channel description"
            notificationManager.createNotificationChannel(channel)
        }

        // 通知の設定を行う
        // NotificationはNotificationCompat.Builderクラスを使って作成する
        val builder = NotificationCompat.Builder(context, "default")
        // ステータスバーに表示されるアイコンのリソースを設定する。
        builder.setSmallIcon(R.drawable.small_icon)
        // 	通知に表示する大きなアイコンをBitmapで指定する。指定されていない場合はsetSmallIconメソッドで指定したリソースが使われる。
        builder.setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.large_icon))
        // いつ表示するか指定する。
        builder.setWhen(System.currentTimeMillis())
        // 通知時の音・バイブ・ライトについて指定する。
        builder.setDefaults(Notification.DEFAULT_ALL)
        // trueの場合はユーザがタップしたら通知が消える。falseの場合はコード上で消す必要がある。
        builder.setAutoCancel(true)

        // EXTRA_TASKからTaskのidを取得して、 idからTaskのインスタンスを取得する
        val taskId = intent!!.getIntExtra(EXTRA_TASK, -1)
        val realm = Realm.getDefaultInstance()
        val task = realm.where(Task::class.java).equalTo("id", taskId).findFirst()

        // タスクの情報を設定する
        builder.setTicker(task!!.title)   // 5.0以降は表示されない
        builder.setContentTitle(task.title)
        builder.setContentText(task.contents)

        // 通知をタップしたら、Intentによってアプリを起動するように設定する
        val startAppIntent = Intent(context, MainActivity::class.java)
        startAppIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
        val pendingIntent = PendingIntent.getActivity(context, 0, startAppIntent, 0)
        builder.setContentIntent(pendingIntent)

        // 通知を表示する
        notificationManager.notify(task!!.id, builder.build())
        realm.close()
    }
}
