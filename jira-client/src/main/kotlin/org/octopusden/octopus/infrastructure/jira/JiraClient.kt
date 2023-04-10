package org.octopusden.octopus.infrastructure.jira

import feign.Headers
import feign.Param
import feign.RequestLine
import org.octopusden.octopus.infrastructure.jira.dto.Assignee
import org.octopusden.octopus.infrastructure.jira.dto.CreateIssueFields
import org.octopusden.octopus.infrastructure.jira.dto.CreateIssueResponse
import org.octopusden.octopus.infrastructure.jira.dto.Issue
import org.octopusden.octopus.infrastructure.jira.dto.UpdateIssueFields

const val REST_API_PATH = "rest/api/2"
const val ISSUE_PATH = "$REST_API_PATH/issue"
const val USER_PATH = "$REST_API_PATH/user"

interface JiraClient {

    @RequestLine("POST $ISSUE_PATH")
    @Headers("Content-Type: application/json")
    fun createIssue(issue: Issue<CreateIssueFields>): CreateIssueResponse

    @Headers("Content-Type: application/json")
    @RequestLine("PUT $ISSUE_PATH/{issueKey}?notifyUsers=false")
    fun updateIssue(@Param("issueKey") issueKey: String, issue: Issue<UpdateIssueFields>)

    @RequestLine("GET $USER_PATH/assignable/search?issueKey={issueKey}&username={username}&maxResults=9999")
    fun getAssignable(@Param("issueKey") issueKey: String, @Param("username") username: String?): List<Assignee>
}
