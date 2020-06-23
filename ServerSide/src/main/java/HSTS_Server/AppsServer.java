package HSTS_Server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import HSTS_Entities.Message;
import HSTS_Entities.Question;
import HSTS_Entities.StudentsExecutedExam;
import HSTS_Entities.TimeExtension;
import HSTS_Entities.Exam;
import HSTS_Entities.ExamForExec;
import HSTS_Entities.ExecutedExam;
import HSTS_Entities.HstsUser;
import ocsf_Server.AbstractServer;
import ocsf_Server.ConnectionToClient;

public class AppsServer extends AbstractServer {

	private static Session session;
	static SessionFactory sessionFactory = getSessionFactory();
	private QuestionController questionController;
	private UserController userController;
	private ExamController examController;
	private ExamExecController examExecController;
	private TimeExtensionController timeExtensionController;
	private ExecutedExamController executedExamController;

	Message serverMsg;

	public AppsServer(int port) {
		super(port);
		questionController = new QuestionController();
		userController = new UserController();
		examController = new ExamController();
		examExecController = new ExamExecController();
		timeExtensionController = new TimeExtensionController();
		executedExamController = new ExecutedExamController();
	}

	@Override
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {

		serverMsg = new Message();

		if (((Message) msg).getAction().equals("user log out")) { // user log out without closing the client
			userController.clientDisconnect(((Message) msg).getUser());
		}

		if (((Message) msg).getAction().equals("user connected")) {
			userController.connectUser(((Message) msg).getUser());
		}

		if (((Message) msg).getAction().equals("client disconnect")) // user log out with closing the client
		{
			userController.clientDisconnect(((Message) msg).getUser());
			try {
				client.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (((Message) msg).getAction().equals("Update time extension requests")) {
			timeExtensionController.updateTimeExtensions(((Message) msg).getTimeExtensionArr());
		}

		if (((Message) msg).getAction().equals("Pull teachers")) {
			serverMsg.setTeachers(userController.getTeachers());
			serverMsg.setAction("Got teachers");

			try {
				client.sendToClient(serverMsg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (((Message) msg).getAction().equals("show time extensions")) // principal
		{
			serverMsg.setTimeExtensionArr(timeExtensionController.getTimeExtensions());
			serverMsg.setAction("got time extensions");
			try {
				client.sendToClient(serverMsg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (((Message) msg).getAction().equals("Pull student's exams")) {
			serverMsg.setExamsByStudent(executedExamController.getStudentsExams(((Message) msg).getUser()));
			serverMsg.setExams(
					examController.getExamsById(executedExamController.getStudentsExamById(((Message) msg).getUser())));
			serverMsg.setAction("Pulled for specific student");
			try {
				client.sendToClient(serverMsg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (((Message) msg).getAction().equals("Pull unchecked exams by teacher")) {
			serverMsg.setExecutedExams(executedExamController.getUncheckedExamsByTeacher(((Message) msg).getUser()));
			serverMsg.setExams(examController
					.getExamsById(executedExamController.getUncheckedTeacherExamsById(((Message) msg).getUser())));
			serverMsg.setAction("Pulled Teacher's executed exams");
			try {
				client.sendToClient(serverMsg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (((Message) msg).getAction().equals("Pull Exams and Questions")) {
			serverMsg.setExams(examController.getExams((Message) msg));
			serverMsg.setQuestions(questionController.getQuestions((Message) msg));
			serverMsg.setAction("Show Exams and Questions");
			try {
				client.sendToClient(serverMsg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (((Message) msg).getAction().equals("Request time extension")) // teacher
		{
			timeExtensionController.addTimeExtentionRequest(((Message) msg).getTimeExtension());
		}

		if (((Message) msg).getAction().equals("Add Exam")) {
			examController.addExam(((Message) msg).getExam());
		}

		if (((Message) msg).getAction().equals("Check for extension")) {
			serverMsg.setExtendTime(timeExtensionController.getTimeExtension(((Message) msg).getExamForExec()));
			serverMsg.setAction("Time extension result");
			timeExtensionController.cancelTimeExtensions(((Message) msg).getExamForExec());

			try {
				client.sendToClient(serverMsg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (((Message) msg).getAction().equals("Submit Student Exam")) {
			executedExamController.checkExam(((Message) msg).getStudentsExecutedExam());
			executedExamController.addStudentExectutedExam(((Message) msg).getStudentsExecutedExam());
		}

		if (((Message) msg).getAction().equals("Submit Student Manual Exam")) {
			executedExamController.addStudentExectutedExam(((Message) msg).getStudentsExecutedExam());
		}

		if (((Message) msg).getAction().equals("Enter code")
				|| ((Message) msg).getAction().equals("Pull exam by examCode")) {
			
			serverMsg.setExam(examExecController.getExamForExec(((Message) msg)));
			serverMsg.setExecutedExam(executedExamController.getExecutedExam(((Message) msg)));

			if (serverMsg.getExam() == null) {
				serverMsg.setAction("Exam code invalid");
			} else {
				if (((Message) msg).getAction().equals("Enter code")) {
					HstsUser teacher = new HstsUser();
					teacher.setUserId(serverMsg.getExecutedExam().getAssignedBy());
					serverMsg.setUser(userController.getSubsAndCourses(teacher));
				}
				serverMsg.setAction("Exam for exec");

			}
			try {
				client.sendToClient(serverMsg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		if (((Message) msg).getAction().equals("Add exam for execution")) {
			examExecController.addExamForExec(((Message) (msg)).getExamForExec());
			executedExamController.addExectutedExam(((Message) msg).getExecutedExam());
		}

		if (((Message) msg).getAction().equals("Pull Exams")) {
			serverMsg.setExams(examController.getExams((Message) msg));
			serverMsg.setAction("Got Exams");

			try {
				client.sendToClient(serverMsg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (((Message) msg).getAction().equals("Show Questions")) {
			serverMsg.setQuestions(questionController.getQuestions((Message) msg));
			serverMsg.setAction("Show Questions");

			try {
				client.sendToClient(serverMsg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (((Message) msg).getAction().equals("Create Question")) {
			questionController.addQuestion(((Message) msg).getQuestion());
		}

		if (((Message) msg).getAction().equals("Login")) {
			HstsUser identifiedUser = userController.identification(((Message) msg).getUser());
			if (identifiedUser == null)
				serverMsg.setAction("Identification failed");
			else {
				serverMsg.setAction("Identification succeed");
				serverMsg.setUser(identifiedUser);
			}

			try {
				client.sendToClient(serverMsg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (((Message) msg).getAction().equals("Pull checked exams by teacher")) {
			serverMsg.setExecutedExams(executedExamController.getCheckedExamsByTeacher(((Message) msg).getUser()));
			serverMsg.setExams(examController
					.getExamsById(executedExamController.getCheckedTeacherExamsById(((Message) msg).getUser())));
			serverMsg.setAction("Pulled Teacher's executed exams");

			try {
				client.sendToClient(serverMsg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (((Message) msg).getAction().equals("Review Executed Exam")) {
			executedExamController.updateExecutedExam(((Message) msg).getExecutedExam());
		}

		if (((Message) msg).getAction().equals("Check exam code - teacher")) {
			if (executedExamController.checkCode(((Message) msg).getExecCode()))
				serverMsg.setAction("code not used");
			else
				serverMsg.setAction("code used");

			try {
				client.sendToClient(serverMsg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	protected synchronized void clientDisconnected(ConnectionToClient client) {
		// TODO Auto-generated method stub

		System.out.println("Client Disconnected.");
		super.clientDisconnected(client);
	}

	@Override
	protected void clientConnected(ConnectionToClient client) {
		super.clientConnected(client);
		System.out.println("Client connected: " + client.getInetAddress());
	}

	private static SessionFactory getSessionFactory() throws HibernateException {
		Configuration configuration = new Configuration();
		// Add ALL of your entities here. You can also try adding a whole package
		configuration.addAnnotatedClass(HstsUser.class);
		configuration.addAnnotatedClass(Exam.class);
		configuration.addAnnotatedClass(Question.class);
		configuration.addAnnotatedClass(ExamForExec.class);
		configuration.addAnnotatedClass(StudentsExecutedExam.class);
		configuration.addAnnotatedClass(TimeExtension.class);
		configuration.addAnnotatedClass(ExecutedExam.class);
		ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
				.applySettings(configuration.getProperties()).build();
		return configuration.buildSessionFactory(serviceRegistry);
	}

	public static void addUsersToDB() {
		try {
			session = sessionFactory.openSession();
			session.beginTransaction();

			// Create Students:
			String passwordInput = "S123";
			SHA3.DigestSHA3 digestSHA3 = new SHA3.Digest256();
			byte[] digest = digestSHA3.digest(passwordInput.getBytes());
			ArrayList<String> subjectsOfStudent = new ArrayList<String>();
			ArrayList<String> coursesOfStudent = new ArrayList<String>();
			subjectsOfStudent.add("Math");
			coursesOfStudent.add("Calculus");

			HstsUser student1 = new HstsUser("123456789", Hex.encodeHexString(digest), 1, null, null, "Opal", false);

			session.save(student1);
			session.flush();

			passwordInput = "S1234";
			digestSHA3 = new SHA3.Digest256();
			digest = digestSHA3.digest(passwordInput.getBytes());

			HstsUser student2 = new HstsUser("987654321", Hex.encodeHexString(digest), 1, null, null, "Linoy", false);
			session.save(student2);
			session.flush();

			passwordInput = "S12345";
			digestSHA3 = new SHA3.Digest256();
			digest = digestSHA3.digest(passwordInput.getBytes());

			HstsUser student3 = new HstsUser("121212121", Hex.encodeHexString(digest), 1, null, null, "Chen", false);
			session.save(student3);
			session.flush();

			passwordInput = "S123456";
			digestSHA3 = new SHA3.Digest256();
			digest = digestSHA3.digest(passwordInput.getBytes());

			HstsUser student4 = new HstsUser("111111111", Hex.encodeHexString(digest), 1, null, null, "Guy", false);
			session.save(student4);
			session.flush();

			passwordInput = "S1234567";
			digestSHA3 = new SHA3.Digest256();
			digest = digestSHA3.digest(passwordInput.getBytes());

			HstsUser student5 = new HstsUser("222222222", Hex.encodeHexString(digest), 1, null, null, "Zachary", false);
			session.save(student5);
			session.flush();

			// Create Teachers:
			ArrayList<String> teacher1Subjects = new ArrayList<String>();
			teacher1Subjects.add("Math");
			teacher1Subjects.add("CS");

			ArrayList<String> teacher1Courses = new ArrayList<String>();
			teacher1Courses.add("Introduction to CS");
			teacher1Courses.add("Calculus");
			teacher1Courses.add("OS");

			passwordInput = "T123";
			digestSHA3 = new SHA3.Digest256();
			digest = digestSHA3.digest(passwordInput.getBytes());

			HstsUser teacher1 = new HstsUser("333333333", Hex.encodeHexString(digest), 2, teacher1Subjects,
					teacher1Courses, "Liel", false);

			session.save(teacher1);
			session.flush();

			ArrayList<String> teacher2Subjects = new ArrayList<String>();
			teacher2Subjects.add("Math");

			ArrayList<String> teacher2Courses = new ArrayList<String>();
			teacher2Courses.add("Algebra 101");
			teacher2Courses.add("Calculus");
			teacher2Courses.add("Introduction to Probability");

			passwordInput = "T1234";
			digestSHA3 = new SHA3.Digest256();
			digest = digestSHA3.digest(passwordInput.getBytes());

			HstsUser teacher2 = new HstsUser("444444444", Hex.encodeHexString(digest), 2, teacher2Subjects,
					teacher2Courses, "Trachel", false);

			session.save(teacher2);
			session.flush();

			ArrayList<String> teacher3Subjects = new ArrayList<String>();
			teacher3Subjects.add("Biology");

			ArrayList<String> teacher3Courses = new ArrayList<String>();
			teacher3Courses.add("Stem Cells");
			teacher3Courses.add("Anatomy");

			passwordInput = "T12345";
			digestSHA3 = new SHA3.Digest256();
			digest = digestSHA3.digest(passwordInput.getBytes());

			HstsUser teacher3 = new HstsUser("555555555", Hex.encodeHexString(digest), 2, teacher3Subjects,
					teacher3Courses, "Yafit", false);

			session.save(teacher3);
			session.flush();

			// Create principal:
			ArrayList<String> principalSubjects = new ArrayList<String>();
			principalSubjects.add("Math");
			principalSubjects.add("CS");
			principalSubjects.add("Biology");

			ArrayList<String> principalCourses = new ArrayList<String>();
			principalCourses.add("Calculus");
			principalCourses.add("Algebra 101");
			principalCourses.add("Introduction to Probability");
			principalCourses.add("Introduction to CS");
			principalCourses.add("Data structures");
			principalCourses.add("OS");
			principalCourses.add("Anatomy");
			principalCourses.add("Stem Cells");
			principalCourses.add("Biostructure");

			passwordInput = "P123";
			digestSHA3 = new SHA3.Digest256();
			digest = digestSHA3.digest(passwordInput.getBytes());

			HstsUser principal = new HstsUser("888888888", Hex.encodeHexString(digest), 3, principalSubjects,
					principalCourses, "Malki", false);

			session.save(principal);
			session.flush();

			session.getTransaction().commit(); // Save everything.
		} catch (Exception exception) {
			if (session != null) {
				session.getTransaction().rollback();
			}
			System.err.println("An error occured, changes have been rolled back.");
			exception.printStackTrace();
		} finally {
			session.close();
		}
	}

	public static void addExamsAndQuestionsToDB() {
		try {
			ExamController examController = new ExamController();
			QuestionController questionController = new QuestionController();
			session = sessionFactory.openSession();
			session.beginTransaction();

			// Create Questions:
			ArrayList<String> answers1 = new ArrayList<String>();
			answers1.add("4");
			answers1.add("6");
			answers1.add("8");
			answers1.add("10");
			Question question1 = new Question("2 * 2 =", answers1, 1, "Algebra 101", "Math");
			questionController.addQuestion(question1);
			session.flush();

			ArrayList<String> answers2 = new ArrayList<String>();
			answers2.add("145");
			answers2.add("124");
			answers2.add("14");
			answers2.add("13");
			Question question2 = new Question("11 + 3 =", answers2, 3, "Algebra 101", "Math");
			questionController.addQuestion(question2);
			session.flush();

			ArrayList<String> answers3 = new ArrayList<String>();
			answers3.add("2.5");
			answers3.add("7");
			answers3.add("10");
			answers3.add("4");
			Question question3 = new Question("3 + 2 / 2 =", answers3, 4, "Algebra 101", "Math");
			questionController.addQuestion(question3);
			session.flush();

			ArrayList<String> answers4 = new ArrayList<String>();
			answers4.add("2");
			answers4.add("512");
			answers4.add("1024");
			answers4.add("1000");
			Question question4 = new Question("2 ^ 10 =", answers4, 3, "Algebra 101", "Math");
			questionController.addQuestion(question4);
			session.flush();

			ArrayList<String> answers5 = new ArrayList<String>();
			answers5.add("72");
			answers5.add("63");
			answers5.add("54");
			answers5.add("45");
			Question question5 = new Question("6 * 9 =", answers5, 3, "Algebra 101", "Math");
			questionController.addQuestion(question5);
			session.flush();

			ArrayList<Question> questionsExam1 = new ArrayList<Question>();
			questionsExam1.add(question1);
			questionsExam1.add(question2);
			questionsExam1.add(question3);
			questionsExam1.add(question4);
			questionsExam1.add(question5);

			ArrayList<String> answers6 = new ArrayList<String>();
			answers6.add("Stem");
			answers6.add("Cell");
			answers6.add("It's a stem cell");
			answers6.add("It's a cell stem");
			Question question6 = new Question("Whats a stem cell?", answers6, 3, "Stem Cells", "Biology");
			questionController.addQuestion(question6);
			session.flush();

			ArrayList<String> answers7 = new ArrayList<String>();
			answers7.add("145");
			answers7.add("124");
			answers7.add("326");
			answers7.add("Infinite");
			Question question7 = new Question("How many cells in a stem", answers7, 2, "Stem Cells", "Biology");
			questionController.addQuestion(question7);
			session.flush();

			ArrayList<String> answers8 = new ArrayList<String>();
			answers8.add("Yes");
			answers8.add("No");
			answers8.add("IDK");
			answers8.add("Yes, but...");
			Question question8 = new Question("Stem cells?", answers8, 1, "Stem Cells", "Biology");
			questionController.addQuestion(question8);
			session.flush();

			ArrayList<Question> questionsExam2 = new ArrayList<Question>();
			questionsExam2.add(question6);
			questionsExam2.add(question7);
			questionsExam2.add(question8);

			ArrayList<String> answers9 = new ArrayList<String>();
			answers9.add("Python");
			answers9.add("Java");
			answers9.add("C++");
			answers9.add("C#");
			Question question9 = new Question("In which of the following the user requires to free its allocated data?",
					answers9, 3, "Introduction to CS", "CS");
			questionController.addQuestion(question9);
			session.flush();

			ArrayList<String> answers10 = new ArrayList<String>();
			answers10.add("Bucket Sort");
			answers10.add("Dripping Sort");
			answers10.add("Bubble Sort");
			answers10.add("Quick Sort");
			Question question10 = new Question("Which of the following sort didnt we learn?", answers10, 2,
					"Introduction to CS", "CS");
			questionController.addQuestion(question10);
			session.flush();

			// Create Questions:
			ArrayList<String> answers11 = new ArrayList<String>();
			answers11.add("Apple");
			answers11.add("Affirmative");
			answers11.add("Aardvark");
			answers11.add("Alcohol");
			Question question11 = new Question("Whats the first word in an english dictionary?", answers11, 3,
					"Introduction to CS", "CS");
			questionController.addQuestion(question11);
			session.flush();

			ArrayList<Question> questionsExam3 = new ArrayList<Question>();
			questionsExam3.add(question9);
			questionsExam3.add(question10);
			questionsExam3.add(question11);

			ArrayList<String> answers12 = new ArrayList<String>();
			answers12.add("1000");
			answers12.add("250");
			answers12.add("375");
			answers12.add("75");
			Question question12 = new Question("2 * 5 ^ 3 =", answers12, 2, "Calculus", "Math");
			questionController.addQuestion(question12);
			session.flush();

			ArrayList<String> answers13 = new ArrayList<String>();
			answers13.add("124.5");
			answers13.add("70");
			answers13.add("76.5");
			answers13.add("75.5");
			Question question13 = new Question("7 + 9 * 8 - (7 / 2) =", answers13, 4, "Calculus", "Math");
			questionController.addQuestion(question13);
			session.flush();

			ArrayList<Question> questionsExam4 = new ArrayList<Question>();
			questionsExam4.add(question12);
			questionsExam4.add(question13);

			ArrayList<String> answers14 = new ArrayList<String>();
			answers14.add("8192");
			answers14.add("2048");
			answers14.add("1024");
			answers14.add("4096");
			Question question14 = new Question("2 ^ 12 =", answers14, 4, "Calculus", "Math");
			questionController.addQuestion(question14);
			session.flush();

			ArrayList<String> answers15 = new ArrayList<String>();
			answers15.add("4");
			answers15.add("2");
			answers15.add("Infinite");
			answers15.add("0");
			Question question15 = new Question("Whats to lim of 2x/x for x to infinite", answers15, 2, "Calculus",
					"Math");
			questionController.addQuestion(question15);
			session.flush();

			ArrayList<Question> questionsExam5 = new ArrayList<Question>();
			questionsExam5.add(question14);
			questionsExam5.add(question15);

			// Create Exams:
			// Exam 1:
			Exam exam1 = new Exam(questionsExam1, "Solve the following: ", "All questions worth the same", "Trachel", 6,
					new ArrayList<Integer>((List.of(20, 20, 20, 20, 20))), "Math", "Algebra 101");

			ArrayList<Exam> exams = new ArrayList<Exam>();
			exams.add(exam1);

			for (Question question : questionsExam1) {
				question.setExams(exams);
			}

			exam1.setQuestions(questionsExam1);
			examController.addExam(exam1);

			// Exam 2
			Exam exam2 = new Exam(questionsExam2, "Solve the following: ", "", "Yafit", 6,
					new ArrayList<Integer>((List.of(40, 20, 40))), "Biology", "Stem Cells");

			exams = new ArrayList<Exam>();
			exams.add(exam2);

			for (Question question : questionsExam2) {
				question.setExams(exams);
			}

			exam2.setQuestions(questionsExam2);
			examController.addExam(exam2);

			// Exam 3
			Exam exam3 = new Exam(questionsExam3, "Solve the following: ", "", "Liel", 6,
					new ArrayList<Integer>((List.of(30, 10, 60))), "CS", "Introduction to CS");

			exams = new ArrayList<Exam>();
			exams.add(exam3);

			for (Question question : questionsExam3) {
				question.setExams(exams);
			}

			exam3.setQuestions(questionsExam3);
			examController.addExam(exam3);

			// Exam 4
			Exam exam4 = new Exam(questionsExam4, "Solve the following: ", "", "Liel", 6,
					new ArrayList<Integer>((List.of(50, 50))), "Math", "Calculus");

			exams = new ArrayList<Exam>();
			exams.add(exam4);

			for (Question question : questionsExam4) {
				question.setExams(exams);
			}

			exam4.setQuestions(questionsExam4);
			examController.addExam(exam4);

			// Exam 5
			Exam exam5 = new Exam(questionsExam5, "Solve the following: ", "", "Trachel", 1,
					new ArrayList<Integer>((List.of(10, 90))), "Math", "Calculus");

			exams = new ArrayList<Exam>();
			exams.add(exam5);

			for (Question question : questionsExam5) {
				question.setExams(exams);
			}

			exam5.setQuestions(questionsExam5);
			examController.addExam(exam5);

			session.getTransaction().commit(); // Save everything.
		} catch (Exception exception) {
			if (session != null) {
				session.getTransaction().rollback();
			}
			System.err.println("An error occured, changes have been rolled back.");
			exception.printStackTrace();
		} finally {
			session.close();
		}
	}

	public static void main(String[] args) throws IOException {

		AppsServer server = new AppsServer(3000);
		server.listen();
		
		addUsersToDB();
		addExamsAndQuestionsToDB();

	}

}
