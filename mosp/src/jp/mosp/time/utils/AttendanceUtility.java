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
package jp.mosp.time.utils;

import java.util.Date;

import jp.mosp.framework.base.BaseDtoInterface;
import jp.mosp.framework.base.MospException;
import jp.mosp.framework.constant.MospConst;
import jp.mosp.framework.instance.InstanceFactory;
import jp.mosp.framework.utils.DateUtility;
import jp.mosp.time.constant.TimeConst;
import jp.mosp.time.dto.settings.AttendanceDtoInterface;
import jp.mosp.time.dto.settings.GoOutDtoInterface;
import jp.mosp.time.dto.settings.RestDtoInterface;
import jp.mosp.time.dto.settings.WorkTypeItemDtoInterface;
import jp.mosp.time.entity.ApplicationEntity;
import jp.mosp.time.entity.RequestEntityInterface;
import jp.mosp.time.entity.WorkTypeEntityInterface;

/**
 * 勤怠登録における有用なメソッドを提供する。<br><br>
 */
public class AttendanceUtility {
	
	/**
	 * 他クラスからのインスタンス化を防止する。<br>
	 */
	private AttendanceUtility() {
		// 処理無し
	}
	
	/**
	 * 始業打刻から勤怠情報に登録する始業時刻を取得する。<br>
	 * <br>
	 * 1.直行の場合：始業予定時刻<br>
	 * 2.遅刻の場合：<br>
	 *   勤務予定時間表示設定が有効である場合：勤怠設定で丸めた打刻時刻<br>
	 *   勤務予定時間表示設定が無効である場合：勤怠設定で丸めた実打刻時刻<br>
	 * 3.勤務予定時間表示設定が有効である場合：勤務前残業を考慮した始業予定時刻<br>
	 * 4.その他の場合：勤怠設定で丸めた実打刻時刻<br>
	 * <br>
	 * @param applicationEntity 設定適用エンティティ
	 * @param requestEntity     申請エンティティ
	 * @param workTypeEntity    勤務形態エンティティ
	 * @param recordTime        打刻時刻
	 * @return 始業時刻
	 * @throws MospException 日付の変換に失敗した場合
	 */
	public static Date getStartTime(ApplicationEntity applicationEntity, RequestEntityInterface requestEntity,
			WorkTypeEntityInterface workTypeEntity, Date recordTime) throws MospException {
		// 始業予定時刻を取得(勤務形態エンティティ及び申請エンティティから)
		Date scheduledTime = workTypeEntity.getStartTime(requestEntity);
		// 勤務前残業自動申請設定(有効の場合TRUE)
		boolean isAutoBeforeOverwork = false;
		
		WorkTypeItemDtoInterface workTypeItemDto = workTypeEntity.getWorkTypeItem(TimeConst.CODE_AUTO_BEFORE_OVERWORK);
		if (workTypeItemDto != null) {
			isAutoBeforeOverwork = workTypeItemDto.getPreliminary()
				.equals(String.valueOf(String.valueOf(MospConst.INACTIVATE_FLAG_OFF)));
			
		}
		
		// 残業申請(勤務前残業)の申請時間(分)を取得
		int overtimeMinutesBeforeWork = requestEntity.getOvertimeMinutesBeforeWork(false);
		
		// 勤務前残業自動申請が有効である場合
		if (isAutoBeforeOverwork) {
			// 勤怠申請情報で前残業申請情報設定
			// 勤務形態の始業時刻-startTimeIntを引いた時間を設定
			int beforeOvertimeInt = DateUtility.getHour(recordTime) * TimeConst.CODE_DEFINITION_HOUR;
			int scheduledTimeInt = DateUtility.getHour(scheduledTime) * TimeConst.CODE_DEFINITION_HOUR;
			overtimeMinutesBeforeWork = scheduledTimeInt - beforeOvertimeInt;
			
		}
		
		// 勤務前残業を考慮した始業予定時刻を取得
		Date overScheduledTime = DateUtility.addMinute(scheduledTime, -overtimeMinutesBeforeWork);
		// 1.直行の場合(勤務形態の直行設定を確認)
		if (workTypeEntity.isDirectStart()) {
			// 始業予定時刻を取得
			return scheduledTime;
		}
		// 2.遅刻の場合
		if (recordTime.compareTo(overScheduledTime) > 0) {
			// 勤務予定時間表示設定が有効で且つ、勤務前残業自動申請が無効の場合
			if (applicationEntity.useScheduledTime() && !isAutoBeforeOverwork) {
				// 勤怠設定で丸めた打刻時刻を取得
				return applicationEntity.getRoundedStartTime(recordTime);
			}
			// 勤怠設定で丸めた実打刻時刻を取得
			return applicationEntity.getRoundedActualStartTime(recordTime);
		}
		// 3.勤務予定時間表示設定が有効である場合、勤務前残業自動申請が無効の場合(勤怠設定の勤務予定時間表示設定を確認)
		if (applicationEntity.useScheduledTime() && !isAutoBeforeOverwork) {
			// 勤務前残業を考慮した始業予定時刻を取得
			return overScheduledTime;
		}
		// 4.その他(勤怠設定で丸めた実打刻時刻を取得)
		return applicationEntity.getRoundedActualStartTime(recordTime);
	}
	
	/**
	 * 勤怠データDTOの複製を作成する。
	 * @param dstDto 複製先勤怠データDTO、nullの場合は、関数内でインスタンスを作成
	 * @param srcDto 複製元勤怠データDTO
	 * @return 複製先勤怠データDTO
	 * @throws MospException インスタンスの生成に失敗した場合
	 */
	public static AttendanceDtoInterface getAttendanceDtoClone(AttendanceDtoInterface dstDto,
			AttendanceDtoInterface srcDto) throws MospException {
		// 複製元がnullの場合は、nullを返す
		if (srcDto == null) {
			return null;
		}
		// 複製先が用意されていなければインスタンスを作成
		if (dstDto == null) {
			dstDto = InstanceFactory.<AttendanceDtoInterface> simplifiedInstance(srcDto.getClass().getName());
		}
		// 継承元を複製
		getBaseDtoClone(dstDto, srcDto);
		// 全項目を複製
		dstDto.setTmdAttendanceId(srcDto.getTmdAttendanceId());
		dstDto.setPersonalId(srcDto.getPersonalId());
		dstDto.setWorkDate(srcDto.getWorkDate());
		dstDto.setTimesWork(srcDto.getTimesWork());
		dstDto.setWorkTypeCode(srcDto.getWorkTypeCode());
		dstDto.setDirectStart(srcDto.getDirectStart());
		dstDto.setDirectEnd(srcDto.getDirectEnd());
		dstDto.setForgotRecordWorkStart(srcDto.getForgotRecordWorkStart());
		dstDto.setNotRecordWorkStart(srcDto.getNotRecordWorkStart());
		dstDto.setStartTime(srcDto.getStartTime());
		dstDto.setActualStartTime(srcDto.getActualStartTime());
		dstDto.setEndTime(srcDto.getEndTime());
		dstDto.setActualEndTime(srcDto.getActualEndTime());
		dstDto.setLateDays(srcDto.getLateDays());
		dstDto.setLateThirtyMinutesOrMore(srcDto.getLateThirtyMinutesOrMore());
		dstDto.setLateLessThanThirtyMinutes(srcDto.getLateLessThanThirtyMinutes());
		dstDto.setLateTime(srcDto.getLateTime());
		dstDto.setActualLateTime(srcDto.getActualLateTime());
		dstDto.setLateThirtyMinutesOrMoreTime(srcDto.getLateThirtyMinutesOrMoreTime());
		dstDto.setLateLessThanThirtyMinutesTime(srcDto.getLateLessThanThirtyMinutesTime());
		dstDto.setLateReason(srcDto.getLateReason());
		dstDto.setLateCertificate(srcDto.getLateCertificate());
		dstDto.setLateComment(srcDto.getLateComment());
		dstDto.setLeaveEarlyDays(srcDto.getLeaveEarlyDays());
		dstDto.setLeaveEarlyThirtyMinutesOrMore(srcDto.getLeaveEarlyThirtyMinutesOrMore());
		dstDto.setLeaveEarlyLessThanThirtyMinutes(srcDto.getLeaveEarlyLessThanThirtyMinutes());
		dstDto.setLeaveEarlyTime(srcDto.getLeaveEarlyTime());
		dstDto.setActualLeaveEarlyTime(srcDto.getActualLeaveEarlyTime());
		dstDto.setLeaveEarlyThirtyMinutesOrMoreTime(srcDto.getLeaveEarlyThirtyMinutesOrMoreTime());
		dstDto.setLeaveEarlyLessThanThirtyMinutesTime(srcDto.getLeaveEarlyLessThanThirtyMinutesTime());
		dstDto.setLeaveEarlyReason(srcDto.getLeaveEarlyReason());
		dstDto.setLeaveEarlyCertificate(srcDto.getLeaveEarlyCertificate());
		dstDto.setLeaveEarlyComment(srcDto.getLeaveEarlyComment());
		dstDto.setWorkTime(srcDto.getWorkTime());
		dstDto.setGeneralWorkTime(srcDto.getGeneralWorkTime());
		dstDto.setWorkTimeWithinPrescribedWorkTime(srcDto.getWorkTimeWithinPrescribedWorkTime());
		dstDto.setContractWorkTime(srcDto.getContractWorkTime());
		dstDto.setShortUnpaid(srcDto.getShortUnpaid());
		dstDto.setRestTime(srcDto.getRestTime());
		dstDto.setOverRestTime(srcDto.getOverRestTime());
		dstDto.setNightRestTime(srcDto.getNightRestTime());
		dstDto.setLegalHolidayRestTime(srcDto.getLegalHolidayRestTime());
		dstDto.setPrescribedHolidayRestTime(srcDto.getPrescribedHolidayRestTime());
		dstDto.setPublicTime(srcDto.getPublicTime());
		dstDto.setPrivateTime(srcDto.getPrivateTime());
		dstDto.setMinutelyHolidayATime(srcDto.getMinutelyHolidayATime());
		dstDto.setMinutelyHolidayBTime(srcDto.getMinutelyHolidayBTime());
		dstDto.setMinutelyHolidayA(srcDto.getMinutelyHolidayA());
		dstDto.setMinutelyHolidayB(srcDto.getMinutelyHolidayB());
		dstDto.setTimesOvertime(srcDto.getTimesOvertime());
		dstDto.setOvertime(srcDto.getOvertime());
		dstDto.setOvertimeBefore(srcDto.getOvertimeBefore());
		dstDto.setOvertimeAfter(srcDto.getOvertimeAfter());
		dstDto.setOvertimeIn(srcDto.getOvertimeIn());
		dstDto.setOvertimeOut(srcDto.getOvertimeOut());
		dstDto.setWorkdayOvertimeIn(srcDto.getWorkdayOvertimeIn());
		dstDto.setWorkdayOvertimeOut(srcDto.getWorkdayOvertimeOut());
		dstDto.setPrescribedHolidayOvertimeIn(srcDto.getPrescribedHolidayOvertimeIn());
		dstDto.setPrescribedHolidayOvertimeOut(srcDto.getPrescribedHolidayOvertimeOut());
		dstDto.setLateNightTime(srcDto.getLateNightTime());
		dstDto.setNightWorkWithinPrescribedWork(srcDto.getNightWorkWithinPrescribedWork());
		dstDto.setNightOvertimeWork(srcDto.getNightOvertimeWork());
		dstDto.setNightWorkOnHoliday(srcDto.getNightWorkOnHoliday());
		dstDto.setSpecificWorkTime(srcDto.getSpecificWorkTime());
		dstDto.setLegalWorkTime(srcDto.getLegalWorkTime());
		dstDto.setDecreaseTime(srcDto.getDecreaseTime());
		dstDto.setTimeComment(srcDto.getTimeComment());
		dstDto.setRemarks(srcDto.getRemarks());
		dstDto.setWorkDays(srcDto.getWorkDays());
		dstDto.setWorkDaysForPaidLeave(srcDto.getWorkDaysForPaidLeave());
		dstDto.setTotalWorkDaysForPaidLeave(srcDto.getTotalWorkDaysForPaidLeave());
		dstDto.setTimesHolidayWork(srcDto.getTimesHolidayWork());
		dstDto.setTimesLegalHolidayWork(srcDto.getTimesLegalHolidayWork());
		dstDto.setTimesPrescribedHolidayWork(srcDto.getTimesPrescribedHolidayWork());
		dstDto.setPaidLeaveDays(srcDto.getPaidLeaveDays());
		dstDto.setPaidLeaveHours(srcDto.getPaidLeaveHours());
		dstDto.setStockLeaveDays(srcDto.getStockLeaveDays());
		dstDto.setCompensationDays(srcDto.getCompensationDays());
		dstDto.setLegalCompensationDays(srcDto.getLegalCompensationDays());
		dstDto.setPrescribedCompensationDays(srcDto.getPrescribedCompensationDays());
		dstDto.setNightCompensationDays(srcDto.getNightCompensationDays());
		dstDto.setSpecialLeaveDays(srcDto.getSpecialLeaveDays());
		dstDto.setSpecialLeaveHours(srcDto.getSpecialLeaveHours());
		dstDto.setOtherLeaveDays(srcDto.getOtherLeaveDays());
		dstDto.setOtherLeaveHours(srcDto.getOtherLeaveHours());
		dstDto.setAbsenceDays(srcDto.getAbsenceDays());
		dstDto.setAbsenceHours(srcDto.getAbsenceHours());
		dstDto.setGrantedLegalCompensationDays(srcDto.getGrantedLegalCompensationDays());
		dstDto.setGrantedPrescribedCompensationDays(srcDto.getGrantedPrescribedCompensationDays());
		dstDto.setGrantedNightCompensationDays(srcDto.getGrantedNightCompensationDays());
		dstDto.setLegalHolidayWorkTimeWithCompensationDay(srcDto.getLegalHolidayWorkTimeWithCompensationDay());
		dstDto.setLegalHolidayWorkTimeWithoutCompensationDay(srcDto.getLegalHolidayWorkTimeWithoutCompensationDay());
		dstDto
			.setPrescribedHolidayWorkTimeWithCompensationDay(srcDto.getPrescribedHolidayWorkTimeWithCompensationDay());
		dstDto.setPrescribedHolidayWorkTimeWithoutCompensationDay(
				srcDto.getPrescribedHolidayWorkTimeWithoutCompensationDay());
		dstDto.setOvertimeInWithCompensationDay(srcDto.getOvertimeInWithCompensationDay());
		dstDto.setOvertimeInWithoutCompensationDay(srcDto.getOvertimeInWithoutCompensationDay());
		dstDto.setOvertimeOutWithCompensationDay(srcDto.getOvertimeOutWithCompensationDay());
		dstDto.setOvertimeOutWithoutCompensationDay(srcDto.getOvertimeOutWithoutCompensationDay());
		dstDto.setStatutoryHolidayWorkTimeIn(srcDto.getStatutoryHolidayWorkTimeIn());
		dstDto.setStatutoryHolidayWorkTimeOut(srcDto.getStatutoryHolidayWorkTimeOut());
		dstDto.setPrescribedHolidayWorkTimeIn(srcDto.getPrescribedHolidayWorkTimeIn());
		dstDto.setPrescribedHolidayWorkTimeOut(srcDto.getPrescribedHolidayWorkTimeOut());
		dstDto.setWorkflow(srcDto.getWorkflow());
		
		return dstDto;
	}
	
	/**
	 * 休憩データDTOの複製を作成する。
	 * @param dstDto 複製先休憩データDTO、nullの場合は、関数内でインスタンスを作成
	 * @param srcDto 複製元休憩データDTO
	 * @return 複製先休憩データDTO
	 * @throws MospException インスタンスの生成に失敗した場合
	 */
	public static RestDtoInterface getRestDtoClone(RestDtoInterface dstDto, RestDtoInterface srcDto)
			throws MospException {
		// 複製元がnullの場合は、nullを返す
		if (srcDto == null) {
			return null;
		}
		// 複製先が用意されていなければインスタンスを作成
		if (dstDto == null) {
			dstDto = InstanceFactory.<RestDtoInterface> simplifiedInstance(srcDto.getClass().getName());
		}
		// 継承元を複製
		getBaseDtoClone(dstDto, srcDto);
		// 全項目を複製
		dstDto.setTmdRestId(srcDto.getTmdRestId());
		dstDto.setPersonalId(srcDto.getPersonalId());
		dstDto.setWorkDate(srcDto.getWorkDate());
		dstDto.setTimesWork(srcDto.getTimesWork());
		dstDto.setRest(srcDto.getRest());
		dstDto.setRestStart(srcDto.getRestStart());
		dstDto.setRestEnd(srcDto.getRestEnd());
		dstDto.setRestTime(srcDto.getRestTime());
		
		return dstDto;
	}
	
	/**
	 * 外出データDTOの複製を作成する。
	 * @param dstDto 複製先外出データDTO、nullの場合は、関数内でインスタンスを作成
	 * @param srcDto 複製元外出データDTO
	 * @return 複製先外出データDTO
	 * @throws MospException インスタンスの生成に失敗した場合
	 */
	public static GoOutDtoInterface getGoOutDtoClone(GoOutDtoInterface dstDto, GoOutDtoInterface srcDto)
			throws MospException {
		// 複製元がnullの場合は、nullを返す
		if (srcDto == null) {
			return null;
		}
		// 複製先が用意されていなければインスタンスを作成
		if (dstDto == null) {
			dstDto = InstanceFactory.<GoOutDtoInterface> simplifiedInstance(srcDto.getClass().getName());
		}
		// 継承元を複製
		getBaseDtoClone(dstDto, srcDto);
		// 全項目を複製
		dstDto.setTmdGoOutId(srcDto.getTmdGoOutId());
		dstDto.setPersonalId(srcDto.getPersonalId());
		dstDto.setWorkDate(srcDto.getWorkDate());
		dstDto.setTimesWork(srcDto.getTimesWork());
		dstDto.setGoOutType(srcDto.getGoOutType());
		dstDto.setTimesGoOut(srcDto.getTimesGoOut());
		dstDto.setGoOutStart(srcDto.getGoOutStart());
		dstDto.setGoOutEnd(srcDto.getGoOutEnd());
		dstDto.setGoOutTime(srcDto.getGoOutTime());
		
		return dstDto;
	}
	
	/**
	 * 共通DTOの複製を作成する。
	 * TODO MospUtilityへ移動？
	 * @param dstDto 複製先共通DTO、nullの場合は、関数内でインスタンスを作成
	 * @param srcDto 複製元共通DTO
	 * @return 複製先共通DTO
	 * @throws MospException インスタンスの生成に失敗した場合
	 */
	public static BaseDtoInterface getBaseDtoClone(BaseDtoInterface dstDto, BaseDtoInterface srcDto)
			throws MospException {
		// 複製元がnullの場合は、nullを返す
		if (srcDto == null) {
			return null;
		}
		// 複製先が用意されていなければインスタンスを作成
		if (dstDto == null) {
			dstDto = InstanceFactory.<BaseDtoInterface> simplifiedInstance(srcDto.getClass().getName());
		}
		// 全項目を複製
		dstDto.setDeleteFlag(srcDto.getDeleteFlag());
		dstDto.setInsertDate(srcDto.getInsertDate());
		dstDto.setInsertUser(srcDto.getInsertUser());
		dstDto.setUpdateDate(srcDto.getUpdateDate());
		dstDto.setUpdateUser(srcDto.getUpdateUser());
		
		return dstDto;
	}
	
}
