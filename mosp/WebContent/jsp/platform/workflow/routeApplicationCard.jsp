<%--
MosP - Mind Open Source Project    http://www.mosp.jp/
Copyright (C) MIND Co., Ltd.       http://www.e-mind.co.jp/

This program is free software: you can redistribute it and/or
modify it under the terms of the GNU Affero General Public License
as published by the Free Software Foundation, either version 3
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>
<%@ page
language = "java"
pageEncoding = "UTF-8"
buffer = "256kb"
autoFlush = "false"
errorPage = "/jsp/common/error.jsp"
%><%@ page
import = "jp.mosp.framework.constant.MospConst"
import = "jp.mosp.framework.base.MospParams"
import = "jp.mosp.framework.utils.HtmlUtility"
import = "jp.mosp.platform.constant.PlatformConst"
import = "jp.mosp.platform.workflow.action.RouteApplicationCardAction"
import = "jp.mosp.platform.workflow.action.RouteApplicationListAction"
import = "jp.mosp.platform.workflow.vo.RouteApplicationCardVo"
import = "jp.mosp.platform.system.constant.PlatformSystemConst"
%><%
MospParams params = (MospParams)request.getAttribute(MospConst.ATT_MOSP_PARAMS);
RouteApplicationCardVo vo = (RouteApplicationCardVo)params.getVo();
%>
<div class="List" id="divEdit">
	<table class="ListTable">
		<tr>
			<th class="EditTableTh" colspan="6">
				<jsp:include page="<%= PlatformSystemConst.PATH_SYSTEM_EDIT_HEADER_JSP %>" flush="false" />
			</th>
		</tr>
		<tr>
			<td class="TitleTd"><span class="RequiredLabel">*&nbsp;</span><%= params.getName("ActivateDate") %></td>
			<td class="InputTd">
				<div id="divSearchSectionCode">
					<input type="text" class="Number4RequiredTextBox" id="txtEditActivateYear" name="txtEditActivateYear" value="<%= HtmlUtility.escapeHTML(vo.getTxtEditActivateYear()) %>" />
					<label for="txtEditActivateYear"><%= params.getName("Year") %></label>
					<input type="text" class="Number2RequiredTextBox" id="txtEditActivateMonth" name="txtEditActivateMonth" value="<%= HtmlUtility.escapeHTML(vo.getTxtEditActivateMonth()) %>" />
					<label for="txtEditActivateMonth"><%= params.getName("Month") %></label>
					<input type="text" class="Number2RequiredTextBox" id="txtEditActivateDay" name="txtEditActivateDay" value="<%= HtmlUtility.escapeHTML(vo.getTxtEditActivateDay()) %>" />
					<label for="txtEditActivateDay"><%= params.getName("Day") %></label>&nbsp;
					<button type="button" class="Name2Button" id="btnActivateDate" onclick="submitForm(event, 'divSearchSectionCode', null, '<%= RouteApplicationCardAction.CMD_SET_ACTIVATION_DATE %>')"><%= vo.getModeActivateDate().equals(PlatformConst.MODE_ACTIVATE_DATE_FIXED) ? params.getName("Change") : params.getName("Decision") %></button>
				</div>
			</td>
			<td class="TitleTd"><span class="RequiredLabel">*&nbsp;</span><label for="txtApplicationCode"><%= params.getName("Route") %><%= params.getName("Apply") %><%= params.getName("Code") %></label></td>
			<td class="InputTd"><input type="text" class="Code10RequiredTextBox" id="txtApplicationCode" name="txtApplicationCode" value="<%= HtmlUtility.escapeHTML(vo.getTxtApplicationCode()) %>"/></td>
			<td class="TitleTd"><span class="RequiredLabel">*&nbsp;</span><label for="txtApplicationName"><%= params.getName("Route") %><%= params.getName("Apply") %><%= params.getName("Name") %></label></td>
			<td class="InputTd"><input type="text" class="Name15RequiredTextBox" id="txtApplicationName" name="txtApplicationName" value="<%= HtmlUtility.escapeHTML(vo.getTxtApplicationName()) %>"/></td>	
		</tr>
		<tr>
			<td class="TitleTd"><span><label for="pltRouteStage"><%= params.getName("Route") %></label></span></td>
			<td class="InputTd">
				<select class="Name15PullDown" id="pltRouteName" name="pltRouteName">
					<%= HtmlUtility.getSelectOption(vo.getAryPltRouteName(), vo.getPltRouteName()) %>
				</select>
			</td>
			<td class="TitleTd"><label for="pltRouteStage"><%= params.getName("Flow") %><%= params.getName("Type") %></label></td>
			<td class="InputTd">
				<select class="Name2PullDown" id="pltFlowType" name="pltFlowType">
					<%= HtmlUtility.getSelectOption(params, PlatformConst.CODE_KEY_WORKFLOW_TYPE, vo.getPltFlowType(), false) %>
				</select>
			</td>
			<td class="TitleTd"><span><label for="pltInactivate"><%= params.getName("Effectiveness") %><%= params.getName("Slash") %><%= params.getName("Inactivate") %></label></span></td>
			<td class="InputTd">
				<select class="Name2PullDown" id="pltEditInactivate" name="pltEditInactivate">
					<%= HtmlUtility.getSelectOption(params, PlatformConst.CODE_KEY_INACTIVATE_FLAG, vo.getPltEditInactivate(), false) %>
				</select>
			</td>
		</tr>
	</table>
	<table class="ListTable">
		<tr>
			<th class="ListTableTh" colspan="5"><span class="TitleTh"><%= params.getName("Route") %><%= params.getName("Apply") %><%= params.getName("Range") %></span><a></a>
			</th>
		</tr>
		<tr>
			<td class="TitleTd" id="tdRadio" rowspan="2">
				<input type="radio" class="RadioButton" name="radioSelect" id="radMaster" value="<%= PlatformConst.APPLICATION_TYPE_MASTER %>" <%= HtmlUtility.getChecked(vo.getRadioSelect().equals(PlatformConst.APPLICATION_TYPE_MASTER)) %> />
			</td>
			<td class="TitleTd" id="tdMasterTitle1"><%= params.getName("WorkPlace") %><%= params.getName("Name") %></td>
			<td class="InputTd">
				<select class="Name15PullDown" id="pltWorkPlace" name="pltWorkPlace">
					<%= HtmlUtility.getSelectOption(vo.getAryPltWorkPlace(), vo.getPltWorkPlace()) %>
				</select>
			</td>
			<td class="TitleTd" id="tdMasterTitle2"><%= params.getName("Employment") %><%= params.getName("Name") %></td>
			<td class="InputTd">
				<select class="Name15PullDown" id="pltEmployment" name="pltEmployment">
					<%= HtmlUtility.getSelectOption(vo.getAryPltEmployment(), vo.getPltEmployment()) %>
				</select>
			</td>
		</tr>
		<tr>
			<td class="TitleTd" id="tdMasterTitle1"><%= params.getName("Section") %><%= params.getName("Name") %></td>
			<td class="InputTd">
				<select class="SectionNamePullDown" id="pltSection" name="pltSection">
					<%= HtmlUtility.getSelectOption(vo.getAryPltSectionMaster(), vo.getPltSection()) %>
				</select>
			</td>
			<td class="TitleTd" id="tdMasterTitle2"><%= params.getName("Position") %><%= params.getName("Name") %></td>
			<td class="InputTd">
				<select class="Name15PullDown" id="pltPosition" name="pltPosition">
					<%= HtmlUtility.getSelectOption(vo.getAryPltPositionMaster(), vo.getPltPosition()) %>
				</select>
			</td>
		</tr>
		<tr>
			<td class="TitleTd" id="tdRadio" rowspan="2">
				<input type="radio" class="RadioButton" name="radioSelect" id="radEmployeeCode" value="<%= PlatformConst.APPLICATION_TYPE_PERSON %>" <%= HtmlUtility.getChecked(vo.getRadioSelect().equals(PlatformConst.APPLICATION_TYPE_PERSON)) %> />
			</td>
			<td class="TitleTd"><%= params.getName("Personal") + params.getName("FrontParentheses") + params.getName("InputCsv") + params.getName("BackParentheses") %></td>
			<td class="InputTd" colspan="3">
			<input type="text" class="CodeCsvTextBox" id="txtEmployeeCode" name="txtEmployeeCode" value="<%= HtmlUtility.escapeHTML(vo.getTxtEmployeeCode()) %>" /></td>
		</tr>
		<tr>
			<td class="TitleTd"><%= params.getName("Suffer") %><%= params.getName("Approver") %><%= params.getName("FullName") %></td>
			<td class="TitleInputTd" colspan="3" id="lblEmployeeName"><%= HtmlUtility.escapeHTML(vo.getLblSectionPosition()) %></td>
		</tr>
	</table>
</div>
<div class="Button">
	<button type="button" class="Name7Button" id="btnRegist" name="btnRegist" onclick="submitRegist(event, 'divEdit', checkExtra, '<%= RouteApplicationCardAction.CMD_REGIST %>')"><%= params.getName("Insert") %></button>
<% if (vo.getModeCardEdit().equals(PlatformConst.MODE_CARD_EDIT_EDIT)) { %>
	<button type="button" class="Name7Button" id="btnDelete" name="btnDelete" onclick="submitDelete(event, null, null, '<%= RouteApplicationCardAction.CMD_DELETE %>')"><%= params.getName("History") %><%= params.getName("Delete") %></button>
<% } %>
	<button type="button" class="Name7Button" id="btnRouteApplicationList" name="btnRouteApplicationList" onclick="submitTransfer(event, null, null, null, '<%= RouteApplicationListAction.CMD_RE_SEARCH %>')"><%= params.getName("Route") %><%= params.getName("Apply") %><%= params.getName("List") %></button>
</div>
