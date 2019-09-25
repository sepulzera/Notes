package de.sepulzera.notes.ds.model;

import android.support.annotation.NonNull;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import de.sepulzera.notes.bf.helper.DateUtil;
import de.sepulzera.notes.bf.helper.JsonUtil;
import de.sepulzera.notes.bf.helper.StringUtil;

/**
 * Repräsentiert eine Notiz.
 */
public class Note implements Serializable {
  public  static final String TAG_NOTE     = "note";

  private static final String TAG_ID       = "_id";

  private static final String TAG_IDENT    = "ident";
  private static final String TAG_REV      = "rev";
  private static final String TAG_CURR_REV = "currrev";
  private static final String TAG_DRAFT    = "draft";

  private static final String TAG_TITLE    = "title";
  private static final String TAG_MSG      = "msg";

  private static final String TAG_DELDT    = "deldt";

  private static final String TAG_CURR     = "curr";
  private static final String TAG_CREADT   = "creadt";
  private static final String TAG_LCHADT   = "lchadt";


  private static final long    ID_DEFAULT       = 0L;
  private static final long    IDENT_DEFAULT    = 0L;
  private static final long    REV_DEFAULT      = 1L;
  private static final boolean CURR_REV_DEFAULT = true;
  private static final boolean DRAFT_DEFAULT    = true;
  private static final String  TITLE_DEFAULT    = "";
  private static final String  MSG_DEFAULT      = "";
  private static final Date    DELDT_DEFAULT    = null;
  private static final boolean CURR_DEFAULT     = true;

  private long mId, mIdent, mRev;
  private boolean mIsCurrRev, mIsDraft;
  private String mTitle, mMsg;
  private Date mDeldt, mCreadt, mLchadt;
  private boolean mCurr;

  /**
   * General purpose Konstruktor.
   */
  public Note() {
    this.mId        = ID_DEFAULT;
    this.mIdent     = IDENT_DEFAULT;
    this.mRev       = REV_DEFAULT;
    this.mIsCurrRev = CURR_REV_DEFAULT;
    this.mIsDraft   = DRAFT_DEFAULT;

    this.mTitle     = TITLE_DEFAULT;
    this.mMsg       = MSG_DEFAULT;

    this.mDeldt     = DELDT_DEFAULT;

    this.mCurr      = CURR_DEFAULT;
    this.mCreadt    = null;
    this.mLchadt    = null;
  }

  public Note(final JSONObject json) {
    this.mId        = JsonUtil.getLongD  (json, TAG_ID        , ID_DEFAULT);
    this.mIdent     = JsonUtil.getLongD  (json, TAG_IDENT     , IDENT_DEFAULT);
    this.mRev       = JsonUtil.getLongD  (json, TAG_REV       , REV_DEFAULT);
    this.mIsCurrRev = JsonUtil.getBoolD  (json, TAG_CURR_REV  , CURR_REV_DEFAULT);
    this.mIsDraft   = JsonUtil.getBoolD  (json, TAG_DRAFT     , DRAFT_DEFAULT);

    this.mTitle     = JsonUtil.getStringD(json, TAG_TITLE     , TITLE_DEFAULT);
    this.mMsg       = JsonUtil.getStringD(json, TAG_MSG       , MSG_DEFAULT);

    this.mDeldt     = JsonUtil.getDateD  (json, TAG_CREADT    , DELDT_DEFAULT);

    this.mCurr      = JsonUtil.getBoolD  (json, TAG_CURR      , CURR_DEFAULT);
    this.mCreadt    = JsonUtil.getDateD  (json, TAG_CREADT    , Calendar.getInstance().getTime());
    this.mLchadt    = JsonUtil.getDateD  (json, TAG_LCHADT    , Calendar.getInstance().getTime());
  }

  public JSONObject toJson() {
    final JSONObject json = new JSONObject();

    JsonUtil.putLong           (json, TAG_ID       , getId());
    JsonUtil.putLong           (json, TAG_IDENT    , getIdent());
    JsonUtil.putLong           (json, TAG_REV      , getRevision());
    JsonUtil.putBool           (json, TAG_CURR_REV , getCurrRev());
    JsonUtil.putBool           (json, TAG_DRAFT    , getDraft());

    JsonUtil.putStringIfPresent(json, TAG_TITLE    , getTitle());
    JsonUtil.putStringIfPresent(json, TAG_MSG      , getMsg());

    JsonUtil.putDateIfPresent  (json, TAG_DELDT    , getDeldt());

    JsonUtil.putBool           (json, TAG_CURR     , getCurr());
    JsonUtil.putDateIfPresent  (json, TAG_CREADT   , getCreadt());
    JsonUtil.putDateIfPresent  (json, TAG_LCHADT   , getLchadt());

    return json;
  }

  @NonNull
  @Override
  public String toString() {
    return "id=" + getId()
        + ",ident=" + getIdent() + ",rev=" + getRevision() + ",draft=" + getDraft()
        + ",currRev=" + getCurrRev() + ",curr=" + getCurr()
        + ",title=" + getTitle() + ",msg=" + getMsg()
        + ",deldt=" + DateUtil.formatDate(getDeldt())
        + ",creadt=" + DateUtil.formatDate(getCreadt()) + ",lchadt=" + DateUtil.formatDate(getLchadt());
  }

  // typ. Getter und Setter

  public long    getId()       { return this.mId; }
  public long    getIdent()    { return this.mIdent; }
  public long    getRevision() { return this.mRev; }
  public boolean getCurrRev()  { return this.mIsCurrRev; }
  public boolean getDraft()    { return this.mIsDraft; }

  public String  getTitle()    { return this.mTitle; }
  public String  getMsg()      { return this.mMsg; }

  public Date    getDeldt()   { return this.mDeldt; }

  public boolean getCurr()     { return this.mCurr; }
  public Date    getCreadt()   { return this.mCreadt; }
  public Date    getLchadt()   { return this.mLchadt; }


  public void setId(long id)                { this.mId = id; }
  public void setIdent(long id)             { this.mIdent = id; }
  public void setRevision(long rev)         { this.mRev = rev; }
  public void setCurrRev(boolean isCurrRev) { this.mIsCurrRev = isCurrRev; }
  public void setDraft(boolean isDraft)     { this.mIsDraft = isDraft; }

  public void setTitle(String title)        { this.mTitle = title; }
  public void setMsg(String msg)            { this.mMsg = StringUtil.defaultIfNull(msg, ""); }

  public void setDeldt(final Date deldt)    { this.mDeldt = deldt; }

  public void setCurr(boolean curr)         { this.mCurr = curr; }
  public void setCreadt(final Date creadt)  { this.mCreadt = creadt; }
  public void setLchadt(final Date lchadt)  { this.mLchadt = lchadt; }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Note note = (Note) o;
    return mIdent == note.mIdent &&
        mRev == note.mRev;
  }

  @Override
  public int hashCode() {
    return Objects.hash(mIdent, mRev);
  }
}
