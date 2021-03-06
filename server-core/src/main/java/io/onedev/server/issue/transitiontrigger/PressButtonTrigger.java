package io.onedev.server.issue.transitiontrigger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.issue.fieldspec.FieldSpec;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.search.entity.issue.IssueQueryLexer;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.util.Usage;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.IssueQuery;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.RoleChoice;

@Editable(order=100, name="Button is pressed")
public class PressButtonTrigger extends TransitionTrigger {

	private static final long serialVersionUID = 1L;

	private String buttonLabel;

	private List<String> authorizedRoles = new ArrayList<>();
	
	private List<String> promptFields = new ArrayList<>();
	
	public PressButtonTrigger() {
		setIssueQuery(io.onedev.server.search.entity.issue.IssueQuery
				.getRuleName(IssueQueryLexer.All));
	}
	
	@Editable(order=100)
	@NotEmpty
	public String getButtonLabel() {
		return buttonLabel;
	}

	public void setButtonLabel(String buttonLabel) {
		this.buttonLabel = buttonLabel;
	}

	@Editable(order=200, description="Optionally specify authorized roles to press this button. "
			+ "If not specified, all users are allowed")
	@RoleChoice
	public List<String> getAuthorizedRoles() {
		return authorizedRoles;
	}
	
	public void setAuthorizedRoles(List<String> authorizedRoles) {
		this.authorizedRoles = authorizedRoles;
	}

	@Editable(order=500, description="Optionally select fields to prompt when this button is pressed")
	@ChoiceProvider("getFieldChoices")
	@NameOfEmptyValue("No fields to prompt")
	public List<String> getPromptFields() {
		return promptFields;
	}

	public void setPromptFields(List<String> promptFields) {
		this.promptFields = promptFields;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getFieldChoices() {
		List<String> fields = new ArrayList<>();
		GlobalIssueSetting issueSetting = OneDev.getInstance(SettingManager.class).getIssueSetting();
		for (FieldSpec field: issueSetting.getFieldSpecs())
			fields.add(field.getName());
		return fields;
	}

	@Override
	public void onRenameField(String oldName, String newName) {
		super.onRenameField(oldName, newName);
		int index = getPromptFields().indexOf(oldName);
		if (index != -1) {
			if (getPromptFields().contains(newName))				
				getPromptFields().remove(index);
			else
				getPromptFields().set(index, newName);
		}
	}

	@Override
	public boolean onDeleteField(String fieldName) {
		if (super.onDeleteField(fieldName))
			return true;
		for (Iterator<String> it = getPromptFields().iterator(); it.hasNext();) {
			if (it.next().equals(fieldName))
				it.remove();
		}
		return false;
	}

	@Override
	public void onRenameRole(String oldName, String newName) {
		int index = getAuthorizedRoles().indexOf(oldName);
		if (index != -1) 
			getAuthorizedRoles().set(index, newName);
	}

	@Override
	public Usage onDeleteRole(String roleName) {
		Usage usage = super.onDeleteRole(roleName);
		if (getAuthorizedRoles().contains(roleName))
			usage.add("authorized roles");
		return usage;
	}

	@Override
	public Collection<String> getUndefinedFields() {
		Collection<String> undefinedFields = super.getUndefinedFields();
		GlobalIssueSetting setting = OneDev.getInstance(SettingManager.class).getIssueSetting();
		for (String field: getPromptFields()) {
			if (setting.getFieldSpec(field) == null)
				undefinedFields.add(field);
		}
		return undefinedFields;
	}

	public boolean isAuthorized(Project project) {
		if (!getAuthorizedRoles().isEmpty()) {
			if (SecurityUtils.canManageIssues(Project.get())) {
				return true;
			} else {
				for (String roleName: getAuthorizedRoles()) {
					if (SecurityUtils.isAuthorizedWithRole(project, roleName))
						return true;
				}
				return false;
			}
		} else {
			return true;
		}
	}

	@Editable(order=1000, name="Applicable Issues", description="Specify criteria of issues applicable for this transition")
	@IssueQuery(withOrder = false, withCurrentUserCriteria = true, withCurrentBuildCriteria = false, 
			withCurrentPullRequestCriteria = false, withCurrentCommitCriteria = false)
	@NotEmpty
	@Override
	public String getIssueQuery() {
		return super.getIssueQuery();
	}

	public void setIssueQuery(String issueQuery) {
		super.setIssueQuery(issueQuery);
	}
	
	@Override
	public String getDescription() {
		if (authorizedRoles.isEmpty())
			return "Button '" + buttonLabel + "' is pressed by any user";
		else
			return "Button '" + buttonLabel + "' is pressed by any user of roles " + authorizedRoles;
	}
	
}
