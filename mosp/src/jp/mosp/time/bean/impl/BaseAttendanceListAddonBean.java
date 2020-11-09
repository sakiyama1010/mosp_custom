/*
 * MosP - Mind Open Source Project    http://www.mosp.jp/
 * Copyright (C) MIND Co., Ltd.       http://www.e-mind.co.jp/
 * 
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jp.mosp.time.bean.impl;

import java.sql.Connection;

import jp.mosp.framework.base.MospException;
import jp.mosp.framework.base.MospParams;
import jp.mosp.time.bean.AttendanceBean;
import jp.mosp.time.bean.AttendanceListAddonBeanInterface;
import jp.mosp.time.bean.AttendanceListReferenceBeanInterface;

/**
 * 勤怠一覧追加処理ベースクラス。<br/>
 * メソッド追加対策として作成。
 * このクラスを継承して、実際の勤怠詳細追加処理を作成しておくことで、作成後のメソッドの追加時、エラーが発生しないようにできる。
 */
public class BaseAttendanceListAddonBean extends AttendanceBean implements AttendanceListAddonBeanInterface {
	
	/**
	 * {@link AttendanceBean#AttendanceBean()}を実行する。<br>
	 */
	public BaseAttendanceListAddonBean() {
		super();
	}
	
	/**
	 * {@link AttendanceBean#AttendanceBean()}を実行する。<br>
	 * @param mospParams 設定するMosP処理情報
	 * @param connection 設定するデータベースコネクション
	 */
	public BaseAttendanceListAddonBean(MospParams mospParams, Connection connection) {
		super(mospParams, connection);
	}
	
	/**
	 * @throws MospException 警告の回避
	 */
	@Override
	public void mapping() throws MospException {
		// 何もしない
	}
	
	/**
	 * @throws MospException 警告の回避
	 */
	@Override
	public void draft() throws MospException {
		// 何もしない
	}
	
	/**
	 * @throws MospException 警告の回避
	 */
	@Override
	public void appli() throws MospException {
		// 何もしない
	}
	
	/**
	 * @throws MospException 警告の回避
	 */
	@Override
	public void approve() throws MospException {
		// 何もしない
	}
	
	/**
	 * @throws MospException 警告の回避
	 */
	@Override
	public void total() throws MospException {
		// 何もしない
	}
	
	/**
	 * @throws MospException 警告の回避
	 */
	@Override
	public void init() throws MospException {
		// 何もしない
	}
	
	/**
	 * @throws MospException 警告の回避
	 */
	@Override
	public void setVoFields(AttendanceListReferenceBeanInterface attendanceListReference) throws MospException {
		// 何もしない
	}
}
