package org.octopusden.octopus.infrastructure.jira

import feign.Headers
import feign.Param
import feign.RequestLine
import org.octopusden.octopus.infrastructure.jira.dto.ActiveSprintResponse
import org.octopusden.octopus.infrastructure.jira.dto.Assignee
import org.octopusden.octopus.infrastructure.jira.dto.CreateIssueFields
import org.octopusden.octopus.infrastructure.jira.dto.CreateIssueResponse
import org.octopusden.octopus.infrastructure.jira.dto.Issue
import org.octopusden.octopus.infrastructure.jira.dto.MoveIssuesToSprintRequest
import org.octopusden.octopus.infrastructure.jira.dto.RemoteLinkRequest
import org.octopusden.octopus.infrastructure.jira.dto.RemoteLinkResponse
import org.octopusden.octopus.infrastructure.jira.dto.SearchIssueResponse
import org.octopusden.octopus.infrastructure.jira.dto.SearchIssueRequest
import org.octopusden.octopus.infrastructure.jira.dto.UpdateIssueFields

const val REST_API_PATH = "rest/api/2"
const val REST_AGILE_PATH = "rest/agile/1.0"
const val ISSUE_PATH = "$REST_API_PATH/issue"
const val USER_PATH = "$REST_API_PATH/user"
const val BOARD_PATH = "$REST_AGILE_PATH/board"
const val SPRINT_PATH = "$REST_AGILE_PATH/sprint"
const val SEARCH_PATH = "$REST_API_PATH/search"
interface JiraClient {

    @RequestLine("POST $ISSUE_PATH")
    @Headers("Content-Type: application/json")
    fun createIssue(issue: Issue<CreateIssueFields>): CreateIssueResponse

    @Headers("Content-Type: application/json")
    @RequestLine("PUT $ISSUE_PATH/{issueKey}?notifyUsers=false")
    fun updateIssue(@Param("issueKey") issueKey: String, issue: Issue<UpdateIssueFields>)

    @RequestLine("GET $USER_PATH/assignable/search?issueKey={issueKey}&username={username}&maxResults=9999")
    fun getAssignable(@Param("issueKey") issueKey: String, @Param("username") username: String?): List<Assignee>

    @Headers("Content-Type: application/json")
    @RequestLine("GET $BOARD_PATH/{boardId}/sprint?state=active")
    fun getActiveSprint(@Param("boardId") boardId: Long): ActiveSprintResponse

    @Headers("Content-Type: application/json")
    @RequestLine("POST $SPRINT_PATH/{sprintId}/issue")
    fun moveIssuesToSprint(@Param("sprintId") sprintId: Long, moveIssuesToSprintRequest: MoveIssuesToSprintRequest): Unit

    @Headers("Content-Type: application/json")
    @RequestLine("POST $ISSUE_PATH/{issueKey}/remotelink")
    fun addRemoteLink(@Param("issueKey") issueKey: String, remoteLinkRequest: RemoteLinkRequest): RemoteLinkResponse

    @Headers("Content-Type: application/json")
    @RequestLine("POST $SEARCH_PATH")
    fun searchIssueWithJql(searchIssueRequest: SearchIssueRequest): SearchIssueResponse

}
