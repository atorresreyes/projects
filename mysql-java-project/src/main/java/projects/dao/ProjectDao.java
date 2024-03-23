package projects.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import projects.entity.Category;
import projects.entity.Material;
import projects.entity.Project;
import projects.entity.Step;
import projects.exception.DbException;
import provided.util.DaoBase;

public class ProjectDao extends DaoBase {
	// Constants
	private static final String CATEGORY_TABLE = "category";
	private static final String MATERIAL_TABLE = "material";
	private static final String PROJECT_TABLE = "project";
	private static final String PROJECT_CATEGORY_TABLE = "project_category";
	private static final String STEP_TABLE = "step";

	public Project insertProject(Project project) {
		// @formatter:off
		String sql = ""
				+ "INSERT INTO " + PROJECT_TABLE + " "
				+ "(project_name, estimated_hours, actual_hours, difficulty, notes) "
				+ "VALUES "
				+ "(?, ?, ?, ?, ?)";
		// @formatter:on

		try (Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);

			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				setParameter(stmt, 1, project.getProjectName(), String.class);
				setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
				setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
				setParameter(stmt, 4, project.getDifficulty(), Integer.class);
				setParameter(stmt, 5, project.getNotes(), String.class);

				stmt.executeUpdate();

				Integer projectId = getLastInsertId(conn, PROJECT_TABLE);
				commitTransaction(conn);

				project.setProjectId(projectId);
				return project;
			} catch (Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		} catch (SQLException e) {
			throw new DbException(e);
		}

	}

	public List<Project> fetchAllProjects() {
		/* "a. Write the SQL statement to return all projects 
		 * not including materials, steps, or categories. 
		 * Order the results by project name." */
		String sql = "SELECT * FROM " + PROJECT_TABLE + " ORDER BY project_name";
		
		/* "b. Add a try-with-resource statement to obtain the Connection object. 
		 * Catch the SQLException in a catch block and rethrow a new DbException, 
		 * passing in the SQLException object. */
		try(Connection conn = DbConnection.getConnection()) {
			// c. Inside the try block, start a new transaction.
			startTransaction(conn);

			/* "d. Add an inner try-with-resource statement to obtain 
			 * the PreparedStatement from the Connection object. 
			 * In a catch block, catch an Exception object. 
			 * Rollback the transaction and throw a new DbException, 
			 * passing in the Exception object as the cause." */
			try(PreparedStatement stmt = conn.prepareStatement(sql)) {
				/* "e. Inside the (currently) innermost try-with-resource statement,
				 * add a try-with-resource statement to obtain a ResultSet 
				 * from the PreparedStatement. 
				 * Include the import statement for ResultSet. 
				 * It is in the java.sql package." */
				try (ResultSet rs = stmt.executeQuery()) {
					/* "f. Inside the new innermost try-with-resource, 
					 * create and return a List of Projects." */
					List<Project> projects = new LinkedList<>();

					
					/* "g. Loop through the result set. Create and assign each result row to a new Project object. Add the Project object to the List of Projects. You can do this by calling the extract method: 
					 * while(rs.next()) {
					 * 		projects.add(extract(rs, Project.class));
					 * 
					 * or by doing it manually like this:"
					 */
					while (rs.next()) {
						Project project = new Project();

						project.setActualHours(rs.getBigDecimal("actual_hours"));
						project.setDifficulty(rs.getObject("difficulty", Integer.class));
						project.setEstimatedHours(rs.getBigDecimal("estimated_hours"));
						project.setNotes(rs.getString("notes"));
						project.setProjectId(rs.getObject("project_id", Integer.class));
						project.setProjectName(rs.getString("project_name"));

						projects.add(project);
					}
					
					return projects;
				}
			}
			//see D
			catch(Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		}
		//see B
		catch(SQLException e) {
			throw new DbException(e);
		}
	}

	public Optional<Project> fetchProjectById(Integer projectId) {
		String sql = "SELECT * FROM " + PROJECT_TABLE + " WHERE project_id = ?";
		
		try(Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);
			
			try {
				Project project = null;
				
				try(PreparedStatement stmt = conn.prepareStatement(sql)) {
					setParameter(stmt, 1, projectId, Integer.class);
					
					try(ResultSet rs = stmt.executeQuery()) {
						if(rs.next()) {
							project = extract(rs, Project.class);
						}
					}
				}
				
				if(Objects.nonNull(project)) {
					project.getMaterials().addAll(fetchMaterialsForProject(conn, projectId));
					project.getSteps().addAll(fetchStepsForProject(conn, projectId));
					project.getCategories().addAll(fetchCategoriesForProject(conn, projectId));
				}
				
				commitTransaction(conn);
				
				return Optional.ofNullable(project);
				
			} catch(Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
			
		} catch(SQLException e) {
			throw new DbException(e);
		}
		
	}

	private List<Material> fetchMaterialsForProject(Connection conn, Integer projectId) throws SQLException {
		String sql = "SELECT * FROM " + MATERIAL_TABLE + " WHERE project_id = ?";
		
		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParameter(stmt, 1, projectId, Integer.class);
			
			try(ResultSet rs = stmt.executeQuery()) {
				List<Material> materials = new LinkedList<>();
				
				while(rs.next()) {
					materials.add(extract(rs, Material.class));
				}
				
				return materials;
			}
		}
	}
	
	private List<Step> fetchStepsForProject(Connection conn, Integer projectId) throws SQLException {
		String sql = "SELECT * FROM " + STEP_TABLE + " WHERE project_id = ?";
		
		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParameter(stmt, 1, projectId, Integer.class);
			
			try(ResultSet rs = stmt.executeQuery()) {
				List<Step> steps = new LinkedList<>();
				
				while(rs.next()) {
					steps.add(extract(rs, Step.class));
				}
				
				return steps;
			}
		}
	}

	private List<Category> fetchCategoriesForProject(Connection conn, Integer projectId) throws SQLException {
		// @formatter:off
		String sql = ""
				+ "SELECT c.* FROM " + CATEGORY_TABLE + " c " 
				+ "JOIN " + PROJECT_CATEGORY_TABLE + " pc USING (category_id) " 
				+ "WHERE project_id = ?";
		// @formater:on
		
		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParameter(stmt, 1, projectId, Integer.class);
			
			try(ResultSet rs = stmt.executeQuery()) {
				List<Category> categories = new LinkedList<>();
				
				while(rs.next()) {
					categories.add(extract(rs, Category.class));
				}
				return categories;
			}
		}
	}

	public boolean modifyProjectDetails(Project project) {
		/* The difference in this method and the insert method
		 * is that you will examine the return value from executeUpdate(). 
		 * The executeUpdate() method returns the number of rows affected by the UPDATE operation. 
		 * Since a single row is being acted on 
		 * (comparing to the primary key in the WHERE clause guarantees this),
		 * the return value should be 1. 
		 * If it is 0 it means that no rows were acted on 
		 * and the primary key value (project ID) is not found. 
		 * So, the method returns true if executeUpdate() returns 1 and false if it returns 0.
		 */
		// @ formatter:off
		String sql = ""
				+ "UPDATE " + PROJECT_TABLE + " SET "
				+ "project_name = ?, "
				+ "estimated_hours = ?, "
				+ "actual_hours = ?, "
				+ "difficulty = ?, "
				+ "notes = ? "
				+ "WHERE project_id = ?";
		// @formatter:on
		
		try(Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);
			
			try(PreparedStatement stmt = conn.prepareStatement(sql)) {
				setParameter(stmt, 1, project.getProjectName(), String.class);
				setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
				setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
				setParameter(stmt, 4, project.getDifficulty(), Integer.class);
				setParameter(stmt, 5, project.getNotes(), String.class);
				setParameter(stmt, 6, project.getProjectId(), Integer.class);
				
				
				//executeUpdate is different in that it RETURNS a value of how many rows were affected
				//we expect that the updated row will be one here
				boolean modified = stmt.executeUpdate() == 1;
				commitTransaction(conn);
				
				return modified;
				
			} catch(Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		} catch(SQLException e) {
			throw new DbException(e);
		}
	}

	public boolean deleteProject(Integer projectId) {
		// @formatter:off
		String sql = ""
				+ "DELETE FROM " + PROJECT_TABLE 
				+ " WHERE project_id = ?";
		// @formatter:on
		
		try(Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);
			
			try(PreparedStatement stmt = conn.prepareStatement(sql)) {
				setParameter(stmt, 1, projectId, Integer.class);

				boolean deleted = stmt.executeUpdate() == 1;
				commitTransaction(conn);
				
				return deleted;
				
			} catch(Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		} catch(SQLException e) {
			throw new DbException(e);
		}
		
				
	}

}
