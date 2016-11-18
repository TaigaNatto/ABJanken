package org.t_robop.abjanken;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nifty.cloud.mb.core.DoneCallback;
import com.nifty.cloud.mb.core.FetchCallback;
import com.nifty.cloud.mb.core.NCMB;
import com.nifty.cloud.mb.core.NCMBException;
import com.nifty.cloud.mb.core.NCMBObject;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    double allAverage;
    int allNum;
    TextView averageText;
    TextView playNum;

    //ダイアログ
    AlertDialog alertDlg;

    //ダイアログのレイアウトを取得するView
    View inputView;
    //dialog内のTextView
    TextView dialogNum;
    TextView dialogResult;
    TextView dialogAve;

    int battleNum;
    int winNum;
    int losNum;
    int halfNum;
    double average;

    ListView aveList;
    ArrayAdapter<String> aveAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mbaas連携
        NCMB.initialize(this.getApplicationContext(),
                "e7e8e34286916b93163a2eaee899528663aa580927a1adcb649c3c872448ccaf",
                "30d951293285b1aa3f200e2768b72b551ce772ade3e8775977ed4351b4552c70");

        setContentView(R.layout.activity_main);

        //ダイアログレイアウトの読み込み
        LayoutInflater factory = LayoutInflater.from(this);
        inputView = factory.inflate(R.layout.janken_dialog_layout, null);
        //ダイアログ内の関連付け
        dialogNum=(TextView)inputView.findViewById(R.id.battle_num);
        dialogResult=(TextView)inputView.findViewById(R.id.battle_result);
        dialogAve=(TextView)inputView.findViewById(R.id.battle_average);

        //mainLayoutの関連付け
        averageText=(TextView)findViewById(R.id.all_ave);
        playNum=(TextView)findViewById(R.id.play_num);
        aveList=(ListView)findViewById(R.id.ave_list);

        aveAdapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        //dialogセット
        setDialog();

        //現在の平均を取得してセット
        getAllAverage();

    }

    //開始が押された時
    public void start(View v){
        //いろんな初期化
        battleNum=0;
        winNum=0;
        losNum=0;
        halfNum=0;
        average=0;//もしかしたらリスト追加に使うかも
        // dialogAve.setText("0.0");
        dialogNum.setText("第1回戦");
        dialogResult.setText("勝敗");
        //外部タッチでdialogが消えないように
        alertDlg.setCanceledOnTouchOutside(false);
        //dialog召喚
        alertDlg.show();
    }

    //ダイアログ内のボタンが押された時
    public void hand(View v){

        int result;

        //このへんでじゃんけん
        result=doJanken(Integer.parseInt(v.getTag().toString()));

        switch (result){
            //勝ち
            case 0:
                dialogResult.setText("勝ち");
                winNum++;
                battleNum++;
                average=(winNum*100)/battleNum;
                dialogAve.setText(String.valueOf(average));
                break;
            //負け
            case 1:
                dialogResult.setText("負け");
                losNum++;
                battleNum++;
                average=(winNum*100)/battleNum;
                dialogAve.setText(String.valueOf(average));
                break;
            //引き分け
            case 2:
                dialogResult.setText("引き分け");
                halfNum++;
                break;
            //エラー
            default:
                dialogResult.setText("エラー");
                break;
        }

        if(battleNum<5){
            dialogNum.setText("第"+(battleNum+1)+"回戦");
        }
        else {
            //サーバーに送る処理
            saveAllData();
            //データ取得
            getAllAverage();
            //list追加
            aveAdapter.add(String.valueOf(average)+"%("+String.valueOf(winNum)+"勝"+String.valueOf(losNum)+"敗"+String.valueOf(halfNum)+"分)");
            aveList.setAdapter(aveAdapter);
            //dialog消す処理
            alertDlg.dismiss();
        }

    }

    //サーバーのデータを取得
    public void getAllAverage(){

        //現在の平均値の取得
        NCMBObject obj = new NCMBObject("Data");
        obj.setObjectId("2ryYSPwlTaKJsu3H");
        obj.fetchInBackground(new FetchCallback<NCMBObject>() {
            @Override
            public void done(NCMBObject object, NCMBException e) {
                if (e != null) {
                    //エラー時の処理
                    Toast.makeText(getApplicationContext(), "接続失敗", Toast.LENGTH_SHORT).show();
                } else {
                    //取得成功時の処理
                    allAverage=object.getDouble("average");
                    allNum=object.getInt("num");
                    Toast.makeText(getApplicationContext(), "データ取得したよ", Toast.LENGTH_SHORT).show();

                    averageText.setText(String.valueOf(allAverage/allNum));
                    playNum.setText(String.valueOf(allNum)+"回プレイされました");
                }
            }
        });
    }

    //サーバーにデータを送る
    public void saveAllData(){

        double calResult=allAverage+average;
        //double calResult=(allAverage+average)/2;

        // クラスのNCMBObjectを作成
        NCMBObject obj = new NCMBObject("Data");
        //更新のためのid指定
        obj.setObjectId("2ryYSPwlTaKJsu3H");
        // オブジェクトの値を設定
        obj.put("num", allNum+1);
        obj.put("average", calResult);
        // データストアへの登録
        obj.saveInBackground(new DoneCallback() {
            @Override
            public void done(NCMBException e) {
                if(e != null){
                    //保存に失敗した場合の処理
                    Toast.makeText(getApplicationContext(), "接続失敗", Toast.LENGTH_SHORT).show();
                }else {
                    //保存に成功した場合の処理
                    Toast.makeText(getApplicationContext(), "データ送ったよ", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void setDialog() {
        if (alertDlg == null) {
            alertDlg = new AlertDialog.Builder(MainActivity.this)                       //ダイアログの生成
                    .setTitle("じゃんけんしようぜ")
                    .setView(inputView)

//                    .setPositiveButton(                                                         //ボタン押された処理
//                            "OK",
//                            new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int which) {
//                                    // OK ボタンクリック処理
//                                    try {
//
//                                    }catch(Exception e){
//
//                                    }
//                                }
//                            })
//                    .setNegativeButton(
//                            "Cancel",
//                            new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int which) {
//                                    // Cancel ボタンクリック処理
//                                }
//                            })

                    // 表示
                    .create();
        }
    }

    //じゃんけんする(0:勝ち　1:負け　2:引き分け)
    public int doJanken(int player){

        int enemy=getEnemy();

        //引き分け
        if(player==enemy){
            return 2;
        }

        switch (player){

            //ぐー
            case 0:
                if(enemy==1){
                    //かち
                    return 0;
                }
                else {
                    //まけ
                    return 1;
                }

            //ちょき
            case 1:
                if(enemy==2){
                    //かち
                    return 0;
                }
                else {
                    //まけ
                    return 1;
                }

            //ぱー
            case 2:
                if(enemy==0){
                    //かち
                    return 0;
                }
                else {
                    //まけ
                    return 1;
                }

            default:
                break;

        }

        return 3;
    }

    //三択の乱数生成
    public int getEnemy(){
        //敵の出し手を決める
        Random r = new Random();
        int n = r.nextInt(3);

        return n;
    }


}
