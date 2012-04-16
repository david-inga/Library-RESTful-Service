package pe.edu.pucp.library;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.googlecode.objectify.Key;

/**
 * @author cgavidia
 * 
 */
public class BooksResource extends ServerResource {

	public static final Logger LOG = Logger.getLogger(BooksResource.class
			.getName());

	private static final String BOOKS_ELEMENT = "libros";
	private static final String POST_ERROR_CODE = "1";
	private static final String POST_ERROR_MSG = "No ha sido posible registrar el libro";
	private static final String BOOK_CREATED_MSG = "El libro se creo con �xito";

	private BookDAO dao = new BookDAO();

	@Post
	public Representation acceptBook(Representation entity) {
		Form form = new Form(entity);
		Representation result = null;

		try {
			setStatus(Status.SUCCESS_CREATED);
			Book book = new Book(form);
			Key<Book> key = dao.add(book);
			result = new StringRepresentation(BOOK_CREATED_MSG,
					MediaType.TEXT_PLAIN);
			result.setLocationRef(getRequest().getResourceRef().getIdentifier()
					+ "/" + key.getId());
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Error in POST", e);
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			result = generateErrorRepresentation(POST_ERROR_MSG + ": "
					+ e.toString(), POST_ERROR_CODE);
		}

		return result;
	}

	private Representation generateErrorRepresentation(String errorMsg,
			String errorCode) {
		DomRepresentation result = null;
		try {
			result = new DomRepresentation(MediaType.TEXT_XML);
			Document document = result.getDocument();
			Element errorElement = document.createElement("error");
			document.appendChild(errorElement);
			Element codeElement = document.createElement(Book.CODE_ELEMENT);
			codeElement.appendChild(document.createTextNode(errorCode));
			errorElement.appendChild(codeElement);

			Element messageElement = document.createElement("mensaje");
			messageElement.appendChild(document.createTextNode(errorMsg));
			errorElement.appendChild(messageElement);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;

	}

	@Get("xml")
	public Representation toXml() {
		try {
			DomRepresentation result = new DomRepresentation(MediaType.TEXT_XML);
			Document document = result.getDocument();
			Element booksElement = document.createElement(BOOKS_ELEMENT);
			document.appendChild(booksElement);
			for (Book book : getBooks()) {
				booksElement.appendChild(book.toXml(document));
			}
			document.normalizeDocument();
			return result;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}

	private List<Book> getBooks() {
		return dao.listByProperty(null, null);
	}

}
