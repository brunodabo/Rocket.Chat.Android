package chat.rocket.android.widget.helper;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ClickableSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * supports only bold, italic, strike.
 */
public class MarkDown {

  /**
   * transform MarkDown text into Spans.
   */
  public static void apply(TextView textView) {
    SpannableString text = new SpannableString(textView.getText());
    removeImage(text);
    highlightLink1(text);
    highlightLink2(text);
    bold(text);
    italic(text);
    strike(text);
    textView.setText(text);
  }

  private static void removeImage(SpannableString inputText) {
    Pattern imagePattern = Pattern.compile(
        "!\\[([^\\]]+)\\]\\(((?:http|https):\\/\\/[^\\)]+)\\)", Pattern.MULTILINE);
    Matcher matcher = imagePattern.matcher(inputText);
    while (matcher.find()) {
      inputText.setSpan(new AbsoluteSizeSpan(0),
          matcher.start(), matcher.end(),
          Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
  }

  private static void highlightLink1(SpannableString inputText) {
    Pattern linkPattern = Pattern.compile(
        "\\[([^\\]]+)\\]\\(((?:http|https):\\/\\/[^\\)]+)\\)", Pattern.MULTILINE);
    final Matcher matcher = linkPattern.matcher(inputText);
    while (matcher.find()) {
      ClickableSpan span = createLinkSpan(matcher.group(2));
      setSpan(span, inputText,
          matcher.start(), matcher.end(),
          1, matcher.group(2).length() + 3);
    }
  }

  private static void highlightLink2(SpannableString inputText) {
    Pattern linkPattern = Pattern.compile(
        "((?:<|&lt;))((?:http|https):\\/\\/[^\\|]+)\\|(.+?)((?=>|&gt;)(?:>|&gt;))", Pattern.MULTILINE);
    Matcher matcher = linkPattern.matcher(inputText);
    while (matcher.find()) {
      ClickableSpan span = createLinkSpan(matcher.group(2));
      setSpan(span, inputText,
          matcher.start(), matcher.end(),
          matcher.group(1).length() + matcher.group(2).length() + 1,
          matcher.group(4).length());
    }
  }

  private static void bold(SpannableString inputText) {
    Pattern boldPattern = Pattern.compile(
        "(^|&gt;|[ >_~`])(\\*{1,2})[^\\*\\r\\n]+(\\*{1,2})([<_~`]|\\B|\\b|$)", Pattern.MULTILINE);
    Matcher matcher = boldPattern.matcher(inputText);
    while (matcher.find()) {
      setSpan(new StyleSpan(Typeface.BOLD), inputText,
          matcher.start() + matcher.group(1).length(),
          matcher.end() - matcher.group(4).length(),
          matcher.group(2).length(),
          matcher.group(3).length());
    }
  }

  private static void italic(SpannableString inputText) {
    Pattern italicPattern = Pattern.compile(
        "(^|&gt;|[ >*~`])(\\_)[^\\_\\r\\n]+(\\_)([<*~`]|\\B|\\b|$)", Pattern.MULTILINE);
    Matcher matcher = italicPattern.matcher(inputText);
    while (matcher.find()) {
      setSpan(new StyleSpan(Typeface.ITALIC), inputText,
          matcher.start() + matcher.group(1).length(),
          matcher.end() - matcher.group(4).length(),
          matcher.group(2).length(),
          matcher.group(3).length());
    }
  }

  private static void strike(SpannableString inputText) {
    Pattern strikePattern = Pattern.compile(
        "(^|&gt;|[ >_*`])(\\~{1,2})[^~\\r\\n]+(\\~{1,2})([<_*`]|\\B|\\b|$)", Pattern.MULTILINE);
    Matcher matcher = strikePattern.matcher(inputText);
    while (matcher.find()) {
      setSpan(new StrikethroughSpan(), inputText,
          matcher.start() + matcher.group(1).length(),
          matcher.end() - matcher.group(4).length(),
          matcher.group(2).length(),
          matcher.group(3).length());
    }
  }

  private static void setSpan(Object span, SpannableString inputText,
                              int start, int end, int markStartLen, int markEndLen) {
    if (markStartLen > 0) {
      inputText.setSpan(new AbsoluteSizeSpan(0),
          start, start + markStartLen,
          Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    inputText.setSpan(span,
        start + markStartLen, end - markEndLen,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

    if (markEndLen > 0) {
      inputText.setSpan(new AbsoluteSizeSpan(0),
          end - markEndLen, end,
          Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
  }

  /*package*/ static ClickableSpan createLinkSpan(final String url) {
    return new ClickableSpan() {
      @Override
      public void onClick(View view) {
        final Context context = view.getContext();
        try {
          Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
          intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          context.startActivity(intent);
          return;
        } catch (Exception exception) {
        }

        try {
          ClipboardManager clipboardManager =
              (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
          clipboardManager.setPrimaryClip(ClipData.newPlainText("linkURL", url));
          Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show();
        } catch (Exception exception) {
        }
      }
    };
  }
}
