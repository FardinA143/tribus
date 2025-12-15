/**
 * <p>El package {@code Response} centralitza el model de domini de les respostes
 * utilitzat per la terminal CLI.</p>
 *
 * <p>Inclou:</p>
 * <ul>
 *   <li>{@link Response.SurveyResponse SurveyResponse}: contenidor principal amb les respostes a una enquesta.</li>
 *   <li>{@link Response.Answer Answer} i els seus subtipus
 *       ({@link Response.TextAnswer}, {@link Response.IntAnswer},
 *       {@link Response.SingleChoiceAnswer}, {@link Response.MultipleChoiceAnswer})
 *       que defineixen els formats de resposta.</li>
 * </ul>
 *
 * <p>Aquest package actua com a punt d'entrada per gestionar les respostes
 * entre controladors, serveis d'import/export i la capa de
 * presentació.</p>
 */
package Response;