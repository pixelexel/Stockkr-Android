package com.akshay.stocks;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.squareup.picasso.Picasso;

public class NewsDialog extends AppCompatDialogFragment {
    private String title;
    private String url;
    private String urlToImage;
    private NewsDialog.NewsDialogListener listener;
    private Context context;

    public NewsDialog(String title, String url, String urlToImage) {
        this.title = title;
        this.url = url;
        this.urlToImage = urlToImage;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_news_dialog, null);

        builder.setView(view);

        TextView textViewNewsMessage = view.findViewById(R.id.text_view_news_dialog_title);
        textViewNewsMessage.setText(title);

        ImageView imageViewNewsDialog = view.findViewById(R.id.image_view_news_dialog);
        Picasso.with(context).load(urlToImage).fit().centerInside().into(imageViewNewsDialog);

        ImageView chrome = (ImageView) view.findViewById(R.id.chrome_button);

        chrome.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
                listener.closeNewsDialog();
            }
        });

        ImageView twitter = (ImageView) view.findViewById(R.id.twitter_button);

        twitter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/intent/tweet?text=Check%20out%20this%20Link:&url==" + url));
                startActivity(browserIntent);
                listener.closeNewsDialog();
            }
        });



        return builder.create();
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (NewsDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException((context.toString()) + "must implement NewsDialogListener");
        }
    }

    public interface NewsDialogListener {
        void closeNewsDialog();
    }

}
