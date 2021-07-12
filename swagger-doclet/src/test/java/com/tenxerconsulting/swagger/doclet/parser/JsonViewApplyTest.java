package com.tenxerconsulting.swagger.doclet.parser;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.RootDoc;
import com.tenxerconsulting.swagger.doclet.apidocs.RootDocLoader;
import com.tenxerconsulting.swagger.doclet.parser.jsonview.Comment;
import com.tenxerconsulting.swagger.doclet.parser.jsonview.CommentThread;
import com.tenxerconsulting.swagger.doclet.parser.jsonview.User;
import com.tenxerconsulting.swagger.doclet.parser.jsonview.Views;

/**
 * The JsonViewApplyTest tests whether fields/methods should be part of a view
 * @version $Id$
 * @author conor.roche
 */
class JsonViewApplyTest {

	@Test
        void testIsItemPartOfView() throws IOException {

		final RootDoc rootDoc = RootDocLoader.fromPath("src/test/java", "com.tenxerconsulting.swagger.doclet.parser.jsonview");

		ClassDoc publicClass = rootDoc.classNamed(Views.PublicClass.class.getName());
		ClassDoc publicInterface = rootDoc.classNamed(Views.PublicInterface.class.getName());
		ClassDoc internalClass = rootDoc.classNamed(Views.InternalClass.class.getName());
		ClassDoc internalInterface = rootDoc.classNamed(Views.InternalInterface.class.getName());
		ClassDoc privateClass = rootDoc.classNamed(Views.PrivateClass.class.getName());
		ClassDoc privateInterface = rootDoc.classNamed(Views.PrivateInterface.class.getName());

		assertNotNull(publicClass);
		assertNotNull(publicInterface);
		assertNotNull(internalClass);
		assertNotNull(internalInterface);
		assertNotNull(privateClass);
		assertNotNull(privateInterface);

		// if public we should only see the public field
		assertTrue(ParserHelper.isItemPartOfView(new ClassDoc[] { publicClass }, new ClassDoc[] { publicClass }));
		assertFalse(ParserHelper.isItemPartOfView(new ClassDoc[] { publicClass }, new ClassDoc[] { publicInterface }));
		assertFalse(ParserHelper.isItemPartOfView(new ClassDoc[] { publicClass }, new ClassDoc[] { internalClass }));
		assertFalse(ParserHelper.isItemPartOfView(new ClassDoc[] { publicClass }, new ClassDoc[] { internalInterface }));
		assertFalse(ParserHelper.isItemPartOfView(new ClassDoc[] { publicClass }, new ClassDoc[] { privateClass }));
		assertFalse(ParserHelper.isItemPartOfView(new ClassDoc[] { publicClass }, new ClassDoc[] { privateInterface }));

		// if internal we should see the internal and public field
		assertTrue(ParserHelper.isItemPartOfView(new ClassDoc[] { internalClass }, new ClassDoc[] { publicClass }));
		assertTrue(ParserHelper.isItemPartOfView(new ClassDoc[] { internalClass }, new ClassDoc[] { internalClass }));
		assertFalse(ParserHelper.isItemPartOfView(new ClassDoc[] { internalClass }, new ClassDoc[] { privateClass }));
		assertTrue(ParserHelper.isItemPartOfView(new ClassDoc[] { internalInterface }, new ClassDoc[] { publicInterface }));
		assertTrue(ParserHelper.isItemPartOfView(new ClassDoc[] { internalInterface }, new ClassDoc[] { internalInterface }));
		assertFalse(ParserHelper.isItemPartOfView(new ClassDoc[] { internalInterface }, new ClassDoc[] { privateInterface }));

		// if private we should see all
		assertTrue(ParserHelper.isItemPartOfView(new ClassDoc[] { privateClass }, new ClassDoc[] { publicClass }));
		assertTrue(ParserHelper.isItemPartOfView(new ClassDoc[] { privateClass }, new ClassDoc[] { internalClass }));
		assertTrue(ParserHelper.isItemPartOfView(new ClassDoc[] { privateClass }, new ClassDoc[] { privateClass }));
		assertTrue(ParserHelper.isItemPartOfView(new ClassDoc[] { privateInterface }, new ClassDoc[] { publicInterface }));
		assertTrue(ParserHelper.isItemPartOfView(new ClassDoc[] { privateInterface }, new ClassDoc[] { internalInterface }));
		assertTrue(ParserHelper.isItemPartOfView(new ClassDoc[] { privateInterface }, new ClassDoc[] { privateInterface }));

	}

	/**
	 * this tests the json view behaviour against the actual model classes for the issue30b test
	 */
	@Test
        void testIsItemPartOfViewIssue30b() throws IOException {

		final RootDoc rootDoc = RootDocLoader.fromPath("src/test/resources", "fixtures.issue30b");

		ClassDoc userView = rootDoc.classNamed("fixtures.issue30b.User$UserView");
		ClassDoc userNameView = rootDoc.classNamed("fixtures.issue30b.User$NameView");
		ClassDoc userCommentView = rootDoc.classNamed("fixtures.issue30b.User$CommentView");
		ClassDoc userHiddenView = rootDoc.classNamed("fixtures.issue30b.User$HiddenView");
		ClassDoc commentCommentView = rootDoc.classNamed("fixtures.issue30b.Comment$CommentView");

		testIssue30bViewInheritance(userView, userNameView, userCommentView, userHiddenView, commentCommentView);
	}

	/**
	 * this tests the json view behaviour of a copy of the model classes for the issue30b test
	 */
	@Test
        void testIsItemPartOfViewIssue30bCopy() throws IOException {

		final RootDoc rootDoc = RootDocLoader.fromPath("src/test/java", "com.tenxerconsulting.swagger.doclet.parser.jsonview");

		ClassDoc userView = rootDoc.classNamed("com.tenxerconsulting.swagger.doclet.parser.jsonview.User$UserView");
		ClassDoc userNameView = rootDoc.classNamed("com.tenxerconsulting.swagger.doclet.parser.jsonview.User$NameView");
		ClassDoc userCommentView = rootDoc.classNamed("com.tenxerconsulting.swagger.doclet.parser.jsonview.User$CommentView");
		ClassDoc userHiddenView = rootDoc.classNamed("com.tenxerconsulting.swagger.doclet.parser.jsonview.User$HiddenView");
		ClassDoc commentCommentView = rootDoc.classNamed("com.tenxerconsulting.swagger.doclet.parser.jsonview.Comment$CommentView");

		testIssue30bViewInheritance(userView, userNameView, userCommentView, userHiddenView, commentCommentView);

	}

	private void testIssue30bViewInheritance(ClassDoc userView, ClassDoc userNameView, ClassDoc userCommentView, ClassDoc userHiddenView,
			ClassDoc commentCommentView) {

		assertNotNull(userView);
		assertNotNull(userNameView);
		assertNotNull(userCommentView);
		assertNotNull(userHiddenView);
		assertNotNull(commentCommentView);

		// test simple inheritance
		assertTrue(ParserHelper.isItemPartOfView(new ClassDoc[] { userView }, new ClassDoc[] { userView }));
		assertTrue(ParserHelper.isItemPartOfView(new ClassDoc[] { userView }, new ClassDoc[] { userNameView }));
		assertTrue(ParserHelper.isItemPartOfView(new ClassDoc[] { userView }, new ClassDoc[] { userCommentView }));

		assertFalse(ParserHelper.isItemPartOfView(new ClassDoc[] { userHiddenView }, new ClassDoc[] { userNameView }));
		assertFalse(ParserHelper.isItemPartOfView(new ClassDoc[] { userHiddenView }, new ClassDoc[] { userCommentView }));

		assertFalse(ParserHelper.isItemPartOfView(new ClassDoc[] { userCommentView }, new ClassDoc[] { userView }));

		// more complex scenario
		assertFalse(ParserHelper.isItemPartOfView(new ClassDoc[] { commentCommentView }, new ClassDoc[] { userCommentView }));
		assertTrue(ParserHelper.isItemPartOfView(new ClassDoc[] { userCommentView }, new ClassDoc[] { commentCommentView }));
		assertTrue(ParserHelper.isItemPartOfView(new ClassDoc[] { userView }, new ClassDoc[] { commentCommentView }));

	}

	/**
	 * This test just confirms the behaviour of jackson for the views used by the issue30b test
	 * TODO The comment model has been revised to not include the user as for me jackson raises an infinite recursion exception, need to check with
	 * \@cviebig how it works for him...
	 */
	@Test
        void testJackson() throws Exception {

		// NOTE the model we test against here is a copy of the ones in src/test/resources/fixtures/issue30b

		User u = new User();
		u.setName("UName");
		u.setComments(new ArrayList<Comment>(1));

		CommentThread t = new CommentThread();
		t.setName("TName");
		t.setComments(new ArrayList<Comment>(1));

		Comment c = new Comment();
		c.setText("Text");
		// c.setUser(u);
		// c.setCommentThread(t);

		u.getComments().add(c);
		t.getComments().add(c);

		ObjectMapper mapper = new ObjectMapper();

		// the user view should output both
		String result = mapper.writerWithView(User.UserView.class).writeValueAsString(u);
		assertTrue(result.contains("name"));
		assertTrue(result.contains("comments"));

		// comment view should output 1
		result = mapper.writerWithView(User.CommentView.class).writeValueAsString(u);
		assertFalse(result.contains("name"));
		assertTrue(result.contains("comments"));

		// another view should output neither
		result = mapper.writerWithView(User.HiddenView.class).writeValueAsString(u);
		assertFalse(result.contains("name"));
		assertFalse(result.contains("comments"));

		// comment.comment view should output neither
		result = mapper.writerWithView(Comment.CommentView.class).writeValueAsString(u);
		assertFalse(result.contains("name"));
		assertFalse(result.contains("comments"));
	}

}
