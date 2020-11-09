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

import java.util.List;

import jp.mosp.framework.base.MospException;
import jp.mosp.time.base.TimeBean;
import jp.mosp.time.bean.AttendanceCalcBeanInterface;
import jp.mosp.time.dto.settings.AttendanceDtoInterface;
import jp.mosp.time.dto.settings.GoOutDtoInterface;
import jp.mosp.time.dto.settings.RestDtoInterface;

/**
 * 勤怠データ自動計算拡張クラス。<br>
 * AttendanceCalcBeanを実行後に処理をしたい場合に追加する。
 */
public class AttendanceCalcExBean extends TimeBean implements AttendanceCalcBeanInterface {
	
	/**
	 * 勤怠計算(日々)処理。<br>
	 */
	AttendanceCalcBeanInterface attendanceClac;
	
	
	@Override
	public void initBean() throws MospException {
		attendanceClac = (AttendanceCalcBeanInterface)createBean(AttendanceCalcBeanInterface.class);
		
	}
	
	@Override
	public void attendanceCalc(AttendanceDtoInterface attendanceDto) throws MospException {
		attendanceClac.attendanceCalc(attendanceDto);
	}
	
	@Override
	public void attendanceCalc(AttendanceDtoInterface attendanceDto, List<RestDtoInterface> restList,
			List<GoOutDtoInterface> publicGoOutList, List<GoOutDtoInterface> privateGoOutList) throws MospException {
		
		attendanceClac.attendanceCalc(attendanceDto, restList, publicGoOutList, privateGoOutList);
		
	}
	
	@Override
	public void calcStartEndTime(AttendanceDtoInterface attendanceDto, boolean useBetweenTime) throws MospException {
		attendanceClac.calcStartEndTime(attendanceDto, useBetweenTime);
		
	}
	
}
