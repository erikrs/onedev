package io.onedev.server.entityquery.codecomment;

import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.entityquery.EntityCriteria;
import io.onedev.server.entityquery.QueryBuildContext;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.CodeCommentConstants;

public class ReplyCriteria extends EntityCriteria<CodeComment> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public ReplyCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<CodeComment> context) {
		From<?, ?> join = context.getJoin(CodeCommentConstants.ATTR_REPLIES);
		Path<String> attribute = join.get(CodeCommentReply.ATTR_CONTENT);
		return context.getBuilder().like(context.getBuilder().lower(attribute), "%" + value.toLowerCase() + "%");
	}

	@Override
	public boolean matches(CodeComment comment) {
		for (CodeCommentReply reply: comment.getReplies()) {
			if (reply.getContent().toLowerCase().contains(value.toLowerCase()))
				return true;
		}
		return false;
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return CodeCommentQuery.quote(CodeCommentConstants.FIELD_REPLY) + " " + CodeCommentQuery.getRuleName(CodeCommentQueryLexer.Contains) + " " + CodeCommentQuery.quote(value);
	}

}
