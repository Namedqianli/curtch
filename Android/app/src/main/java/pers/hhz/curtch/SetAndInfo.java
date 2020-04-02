package pers.hhz.curtch;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

public class SetAndInfo extends Fragment implements View.OnClickListener{

    private TextView textViewContact1;
    private TextView textViewContact2;
    private TextView textViewContact3;
    private TextView textViewContact4;
    private TextView textViewContact5;
    private String sosContact1Number;
    private String sosContact2Number;
    private String sosContact3Number;
    private String sosContact4Number;
    private String sosContact5Number;
    private Context context;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_set_info, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().findViewById(R.id.qmuiBtnSetContact1).setOnClickListener(this);
        getActivity().findViewById(R.id.qmuiBtnSetContact2).setOnClickListener(this);
        getActivity().findViewById(R.id.qmuiBtnSetContact3).setOnClickListener(this);
        getActivity().findViewById(R.id.qmuiBtnSetContact4).setOnClickListener(this);
        getActivity().findViewById(R.id.qmuiBtnSetContact5).setOnClickListener(this);
        textViewContact1 = getActivity().findViewById(R.id.textViewContact1);
        textViewContact2 = getActivity().findViewById(R.id.textViewContact2);
        textViewContact3 = getActivity().findViewById(R.id.textViewContact3);
        textViewContact4 = getActivity().findViewById(R.id.textViewContact4);
        textViewContact5 = getActivity().findViewById(R.id.textViewContact5);
        String spFileName = getResources().getString(R.string.shared_preferences_file_name);
        String sosContact1NumberKey = getResources().getString(R.string.sos_contact1_number);
        String sosContact2NumberKey = getResources().getString(R.string.sos_contact2_number);
        String sosContact3NumberKey = getResources().getString(R.string.sos_contact3_number);
        String sosContact4NumberKey = getResources().getString(R.string.sos_contact4_number);
        String sosContact5NumberKey = getResources().getString(R.string.sos_contact5_number);
        sharedPreferences = getActivity().getSharedPreferences(spFileName, getActivity().MODE_PRIVATE);
        sosContact1Number = sharedPreferences.getString(sosContact1NumberKey, null);
        sosContact2Number = sharedPreferences.getString(sosContact2NumberKey, null);
        sosContact3Number = sharedPreferences.getString(sosContact3NumberKey, null);
        sosContact4Number = sharedPreferences.getString(sosContact4NumberKey, null);
        sosContact5Number = sharedPreferences.getString(sosContact5NumberKey, null);
        if(sosContact1Number != null && !sosContact1Number.isEmpty()){
            ((MyApplication)getActivity().getApplication()).setSosNumber1(sosContact1Number);
            textViewContact1.setText(sosContact1Number);
        }
        if(sosContact2Number != null && !sosContact2Number.isEmpty()){
            ((MyApplication)getActivity().getApplication()).setSosNumber2(sosContact2Number);
            textViewContact2.setText(sosContact2Number);
        }
        if(sosContact3Number != null && !sosContact3Number.isEmpty()){
            ((MyApplication)getActivity().getApplication()).setSosNumber3(sosContact3Number);
            textViewContact3.setText(sosContact3Number);
        }
        if(sosContact4Number != null && !sosContact4Number.isEmpty()){
            ((MyApplication)getActivity().getApplication()).setSosNumber4(sosContact4Number);
            textViewContact4.setText(sosContact4Number);
        }
        if(sosContact5Number != null && !sosContact5Number.isEmpty()){
            ((MyApplication)getActivity().getApplication()).setSosNumber5(sosContact5Number);
            textViewContact5.setText(sosContact5Number);
        }
    }

    private void dealCheckSetSosNumber(final View view){
        final QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(getActivity());
        builder.setTitle("请输入联系人电话")
                .setPlaceholder("联系人电话")
                .setInputType(InputType.TYPE_NUMBER_VARIATION_NORMAL)
                .addAction("取消", new QMUIDialogAction.ActionListener(){

                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction("确定", new QMUIDialogAction.ActionListener(){

                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        final CharSequence text = builder.getEditText().getText();
                        if(text != null && text.length() > 0) {
                            String sosContact1NumberKey = getResources().getString(R.string.sos_contact1_number);
                            String sosContact2NumberKey = getResources().getString(R.string.sos_contact2_number);
                            String sosContact3NumberKey = getResources().getString(R.string.sos_contact3_number);
                            String sosContact4NumberKey = getResources().getString(R.string.sos_contact4_number);
                            String sosContact5NumberKey = getResources().getString(R.string.sos_contact5_number);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            switch (view.getId()){
                                case R.id.qmuiBtnSetContact1:
                                    ((MyApplication)getActivity().getApplication()).setSosNumber1(text.toString());
                                    textViewContact1.setText(text.toString());
                                    editor.putString(sosContact1NumberKey, text.toString());
                                    break;
                                case R.id.qmuiBtnSetContact2:
                                    ((MyApplication)getActivity().getApplication()).setSosNumber2(text.toString());
                                    textViewContact2.setText(text.toString());
                                    editor.putString(sosContact2NumberKey, text.toString());
                                    break;
                                case R.id.qmuiBtnSetContact3:
                                    ((MyApplication)getActivity().getApplication()).setSosNumber3(text.toString());
                                    textViewContact3.setText(text.toString());
                                    editor.putString(sosContact3NumberKey, text.toString());
                                    break;
                                case R.id.qmuiBtnSetContact4:
                                    ((MyApplication)getActivity().getApplication()).setSosNumber4(text.toString());
                                    textViewContact4.setText(text.toString());
                                    editor.putString(sosContact4NumberKey, text.toString());
                                    break;
                                case R.id.qmuiBtnSetContact5:
                                    ((MyApplication)getActivity().getApplication()).setSosNumber5(text.toString());
                                    textViewContact5.setText(text.toString());
                                    editor.putString(sosContact5NumberKey, text.toString());
                                    break;
                            }
                            editor.apply();
                            Toast.makeText(getContext(), "设置成功", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }else{
                            Toast.makeText(getActivity(), "请输入电话", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.qmuiBtnSetContact1:
                dealCheckSetSosNumber(view);
                break;
            case R.id.qmuiBtnSetContact2:
                dealCheckSetSosNumber(view);
                break;
            case R.id.qmuiBtnSetContact3:
                dealCheckSetSosNumber(view);
                break;
            case R.id.qmuiBtnSetContact4:
                dealCheckSetSosNumber(view);
                break;
            case R.id.qmuiBtnSetContact5:
                dealCheckSetSosNumber(view);
                break;
        }
    }
}
